package se.zgodi.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import se.zgodi.dto.invoice.*;

import java.util.List;

@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AccountResource {
    private static final Logger LOG = Logger.getLogger(AccountResource.class);

    @GET
    public Uni<RestResponse<List<AccountResponse>>> get() {
        return AccountDTO
                .listAll(Sort.by("id", Sort.Direction.Descending))
                .map(items -> RestResponse.ok(
                        items.stream().map(entity -> new AccountResponse((AccountDTO) entity)).toList()));
    }

    /**
     * Return account data with list of transactions
     */
    @GET
    @Path("/{id}")
    public Uni<RestResponse<AccountResponse>> getSingle(Long id) {
        return AccountDTO.findById(id)
                .onItem().ifNotNull().transform(account -> RestResponse.ok(new AccountResponse((AccountDTO) account)))
                .onItem().ifNull().continueWith(RestResponse::notFound);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<AccountResponse>> create(AccountRequest accountRequest) {
        AccountDTO account = new AccountDTO(accountRequest);
        return Panache.withTransaction(account::persist).onItem()
                .transform(entityBase -> new AccountResponse((AccountDTO) entityBase))
                .map(persistedItem -> RestResponse.status(RestResponse.Status.CREATED, persistedItem));

    }

    @DELETE
    @Path("/{id}")
    public Uni<RestResponse<Void>> delete(Long id) {
        return Panache.withTransaction(() -> AccountDTO.deleteById(id))
                .map(deleted -> deleted
                        ? RestResponse.ok()
                        : RestResponse.notFound());
    }

    /*
     * Function deletes transaction and updates account balance
     */
    @DELETE
    @Path("/{accountId}/transaction/{transactionId}")
    public Uni<RestResponse<Void>> deleteTransaction(
            @PathParam("accountId") Long accountId,
            @PathParam("transactionId") Long transactionId) {
        return Panache.withTransaction(() -> TransactionDTO.findById(transactionId)
                .onItem().ifNotNull().transformToUni(transaction -> {
                    TransactionDTO txn = (TransactionDTO) transaction;
                    if (!txn.account.id.equals(accountId)) {
                        return Uni.createFrom().failure(new WebApplicationException("Account ID mismatch", 400));
                    }
                    AccountDTO account = txn.account;
                    account.balance = account.balance.subtract(txn.amount);

                    // Persist the updated account before deleting the transaction
                    return account.persist()
                            .onItem().transformToUni(ignored -> txn.delete())
                            .onItem().ifNotNull().transform(
                                    persistedItem -> RestResponse.status(RestResponse.Status.ACCEPTED, persistedItem));
                })
                .onItem().ifNull().failWith(new WebApplicationException("Transaction not found", 404)))
                .onFailure().recoverWithItem(throwable -> {
                    // Handling internal server error
                    throwable.printStackTrace();
                    return RestResponse.status(RestResponse.Status.BAD_REQUEST);
                });
    }

    @POST
    @Path("/{id}/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<TransactionResponse>> addTransaction(@PathParam("id") Long id,
            TransactionRequest transactionRequest) {

        return AccountDTO.findById(id)
                .onItem().ifNotNull().transformToUni(account -> {
                    TransactionDTO transaction = new TransactionDTO(transactionRequest);
                    transaction.tags = transactionRequest.tags.stream()
                            .map(tagName -> new TransactionTagDTO(transaction, tagName)).toList();
                    transaction.account = (AccountDTO) account;
                    ((AccountDTO) account).balance = ((AccountDTO) account).balance.add(transaction.amount);
                    
                    if (transactionRequest.items != null && !transactionRequest.items.isEmpty()) {
                        transaction.items = transactionRequest.items.stream()
                                .map(itemRequest -> {
                                    TransactionItemDTO item = new TransactionItemDTO(itemRequest);
                                    item.transaction = transaction;
                                    return item;
                                }).toList();
                    }
                    
                    return Panache.withTransaction(transaction::persist).onItem()
                            .transform(entityBase -> new TransactionResponse((TransactionDTO) entityBase))
                            .map(response -> RestResponse.status(RestResponse.Status.CREATED, response));
                })
                .onItem().ifNull().continueWith(RestResponse.notFound());
    }

    /**
     * Return account data with list of transactions
     */
    @GET
    @Path("/{id}/transaction")
    public Uni<RestResponse<List<TransactionResponse>>> getTransactionsForAccount(@PathParam("id") Long id) {
        LOG.debug(id);
        return AccountDTO.findById(id)
                .onItem().ifNotNull().transformToUni(account -> {
                    return TransactionDTO.find(
                            "account.id = ?1",
                            Sort.by("id", Sort.Direction.Descending),
                            id)
                            .list()
                            .map(transactions -> RestResponse.ok(
                                    transactions.stream()
                                            .map(transaction -> new TransactionResponse((TransactionDTO) transaction))
                                            .toList()));
                })
                .onItem().ifNull().continueWith(RestResponse.notFound());
    }
}
