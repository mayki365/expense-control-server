package se.zgodi.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import se.zgodi.dto.invoice.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
        LOG.infof("Deleting transaction with ID: %d for account ID: %d", transactionId, accountId);

        return Panache.withTransaction(() -> TransactionDTO.<TransactionDTO>findById(transactionId)
                .onItem().ifNull()
                .failWith(() -> new WebApplicationException("Transaction not found", Response.Status.NOT_FOUND))
                .<RestResponse<Void>>flatMap(transaction -> {
                    TransactionDTO txn = (TransactionDTO) transaction;
                    // Verify the transaction belongs to the specified account
                    if (txn.account == null || !txn.account.id.equals(accountId)) {
                        String message = String.format("Transaction %d does not belong to account %d",
                                transactionId, accountId);
                        throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
                    }

                    // Store the transaction amount and account for update
                    BigDecimal transactionAmount = txn.amount;
                    AccountDTO account = txn.account;

                    // Update account balance
                    account.balance = account.balance.subtract(transactionAmount);

                    // First persist the updated account balance
                    return account.persist()
                            .flatMap(updatedAccount -> {
                                // Delete tags and items first
                                return TransactionDTO
                                        .delete("DELETE FROM TransactionTagDTO t WHERE t.transaction.id = ?1", txn.id)
                                        .chain(() -> TransactionDTO.delete(
                                                "DELETE FROM TransactionItemDTO t WHERE t.transaction.id = ?1", txn.id))
                                        .chain(() -> TransactionDTO
                                                .delete("DELETE FROM TransactionDTO t WHERE t.id = ?1", txn.id))
                                        .map(deleted -> RestResponse.noContent());
                            });
                })).onFailure().recoverWithItem(failure -> {
                    LOG.error("Error deleting transaction", failure);
                    if (failure instanceof WebApplicationException) {
                        WebApplicationException wae = (WebApplicationException) failure;
                        return RestResponse.status(wae.getResponse().getStatus());
                    }
                    return RestResponse.serverError();
                });
    }

    /**
     * Get a specific transaction by ID for an account
     */
    @GET
    @Path("/{accountId}/transaction/{transactionId}")
    public Uni<RestResponse<TransactionResponse>> getTransaction(
            @PathParam("accountId") Long accountId,
            @PathParam("transactionId") Long transactionId) {
        LOG.infof("Getting transaction %d for account %d", transactionId, accountId);

        return AccountDTO.<AccountDTO>findById(accountId)
                .onItem().ifNull()
                .failWith(() -> new WebApplicationException("Account not found", Response.Status.NOT_FOUND))
                .<RestResponse<TransactionResponse>>flatMap(account -> TransactionDTO
                        .<TransactionDTO>findById(transactionId)
                        .onItem().ifNull()
                        .failWith(() -> new WebApplicationException("Transaction not found", Response.Status.NOT_FOUND))
                        .map(transaction -> {
                            TransactionDTO txn = (TransactionDTO) transaction;
                            // Verify the transaction belongs to the specified account
                            if (txn.account == null || !txn.account.id.equals(accountId)) {
                                throw new WebApplicationException(
                                        String.format("Transaction %d does not belong to account %d",
                                                transactionId, accountId),
                                        Response.Status.BAD_REQUEST);
                            }
                            return RestResponse.ok(new TransactionResponse(txn));
                        }))
                .onFailure().recoverWithItem(failure -> {
                    LOG.error("Error getting transaction", failure);
                    if (failure instanceof WebApplicationException) {
                        WebApplicationException wae = (WebApplicationException) failure;
                        return RestResponse.status(wae.getResponse().getStatus());
                    }
                    return RestResponse.serverError();
                });
    }

    /**
     * Update an existing transaction
     */
    @PUT
    @Path("/{accountId}/transaction/{transactionId}")
    public Uni<RestResponse<TransactionResponse>> updateTransaction(
            @PathParam("accountId") Long accountId,
            @PathParam("transactionId") Long transactionId,
            TransactionRequest transactionRequest) {
        LOG.infof("Updating transaction %d for account %d", transactionId, accountId);

        updateTransactionSum(transactionRequest);

        return Panache.withTransaction(() ->
        // First get the transaction with tags
        TransactionDTO
                .<TransactionDTO>find("FROM TransactionDTO t LEFT JOIN FETCH t.tags WHERE t.id = ?1", transactionId)
                .firstResult()
                .onItem().ifNull()
                .failWith(() -> new WebApplicationException("Transaction not found", Response.Status.NOT_FOUND))
                // Then fetch items in a separate query
                .chain(transaction -> {
                    TransactionDTO txn = (TransactionDTO) transaction;
                    return TransactionDTO
                            .<TransactionDTO>find("FROM TransactionDTO t LEFT JOIN FETCH t.items WHERE t.id = ?1",
                                    transactionId)
                            .firstResult()
                            .map(transactionWithItems -> {
                                // Merge items into our transaction
                                txn.items = ((TransactionDTO) transactionWithItems).items;
                                return txn;
                            });
                })
                .<RestResponse<TransactionResponse>>flatMap(transaction -> {
                    TransactionDTO txn = (TransactionDTO) transaction;

                    // Verify the transaction belongs to the specified account
                    if (txn.account == null || !txn.account.id.equals(accountId)) {
                        String message = String.format("Transaction %d does not belong to account %d",
                                transactionId, accountId);
                        throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
                    }

                    // Calculate the difference in amount to update account balance
                    BigDecimal amountDifference = transactionRequest.amount.subtract(txn.amount);

                    // Update transaction fields
                    txn.amount = transactionRequest.amount;
                    txn.name = transactionRequest.name;
                    txn.description = transactionRequest.description;
                    txn.eventDate = transactionRequest.eventDate;
                    txn.bankStatementDate = transactionRequest.bankStatementDate;

                    // Update tags
                    txn.tags.clear();
                    if (transactionRequest.tags != null) {
                        txn.tags.addAll(transactionRequest.tags.stream()
                                .map(tag -> new TransactionTagDTO(txn, tag))
                                .collect(Collectors.toList()));
                    }

                    // Update items
                    txn.items.clear();
                    if (transactionRequest.items != null) {
                        txn.items.addAll(transactionRequest.items.stream()
                                .map(item -> {
                                    TransactionItemDTO itemDTO = new TransactionItemDTO(item);
                                    return itemDTO;
                                })
                                .collect(Collectors.toList()));
                    }

                    // Update account balance with the difference
                    AccountDTO account = txn.account;
                    account.balance = account.balance.add(amountDifference);

                    // Persist changes
                    return account.persist()
                            .chain(() -> txn.persist())
                            .map(updated -> RestResponse.ok(new TransactionResponse(txn)));
                })).onFailure().recoverWithItem(failure -> {
                    LOG.error("Error updating transaction", failure);
                    if (failure instanceof WebApplicationException) {
                        WebApplicationException wae = (WebApplicationException) failure;
                        return RestResponse.status(wae.getResponse().getStatus());
                    }
                    return RestResponse.serverError();
                });
    }

    private void updateTransactionSum(TransactionRequest transactionRequest) {
        transactionRequest.amount = BigDecimal.ZERO;
        if (transactionRequest.items != null && !transactionRequest.items.isEmpty()) {
            transactionRequest.items.forEach(item -> {
                transactionRequest.amount = transactionRequest.amount.add(item.amount);
            });
        }
    }

    @POST
    @Path("/{id}/transaction")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<TransactionResponse>> addTransaction(@PathParam("id") Long id,
            TransactionRequest transactionRequest) {

        updateTransactionSum(transactionRequest);

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
                                    TransactionItemDTO item = new TransactionItemDTO();
                                    item.description = itemRequest.description;
                                    item.amount = itemRequest.amount;
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
