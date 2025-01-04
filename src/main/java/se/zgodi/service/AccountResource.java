package se.zgodi.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import se.zgodi.dto.invoice.AccountDTO;
import se.zgodi.dto.invoice.AccountRequest;
import se.zgodi.dto.invoice.AccountResponse;

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
                        items.stream().map(entity -> new AccountResponse((AccountDTO) entity)).toList())
                );
    }

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
}
