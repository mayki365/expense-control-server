package se.zgodi.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import se.zgodi.dto.invoice.TransactionDTO;
import se.zgodi.dto.invoice.TransactionRequest;
import se.zgodi.dto.invoice.TransactionResponse;
import se.zgodi.dto.invoice.TransactionTagDTO;

import java.util.List;

@Path("/transaction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TransactionResource {

    private static final Logger LOG = Logger.getLogger(TransactionResource.class);

    @GET
    public Uni<RestResponse<List<TransactionResponse>>> get() {
        return TransactionDTO
                .listAll(Sort.by("id", Sort.Direction.Descending))
                .map(items -> RestResponse.ok(
                        items.stream().map(entity -> new TransactionResponse((TransactionDTO) entity)).toList())
                );
    }

    @GET
    @Path("/{id}")
    public Uni<RestResponse<TransactionResponse>> getSingle(Long id) {
        return TransactionDTO.findById(id).onItem().ifNotNull().transformToUni(transaction ->
            Mutiny.fetch(((TransactionDTO)transaction).tags).map(tags -> RestResponse.ok(new TransactionResponse((TransactionDTO) transaction)))
        );
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<TransactionResponse>> create(TransactionRequest transactionRequest) {
        TransactionDTO transaction = new TransactionDTO(transactionRequest);
        transaction.tags = transactionRequest.tags.stream().map(tagName -> new TransactionTagDTO(transaction, tagName)).toList();

        return Panache.withTransaction(transaction::persist).onItem()
                .transform(entityBase -> new TransactionResponse((TransactionDTO) entityBase))
                .map(persistedItem -> RestResponse.status(RestResponse.Status.CREATED, persistedItem));
    }

}
