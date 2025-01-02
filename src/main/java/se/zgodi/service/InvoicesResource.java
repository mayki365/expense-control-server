package se.zgodi.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.RestResponse;
import se.zgodi.dto.invoice.InvoiceDTO;
import se.zgodi.dto.invoice.InvoiceRequest;
import se.zgodi.dto.invoice.InvoiceResponse;
import se.zgodi.dto.invoice.InvoiceTagDTO;

import java.util.List;
import org.jboss.logging.Logger;

@Path("/invoices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class InvoicesResource {

    private static final Logger LOG = Logger.getLogger(InvoicesResource.class);

    @GET
    public Uni<RestResponse<List<InvoiceResponse>>> get() {
        return InvoiceDTO
                .listAll(Sort.by("id", Sort.Direction.Descending))
                .map(items -> RestResponse.ok(
                        items.stream().map(entity -> new InvoiceResponse((InvoiceDTO) entity)).toList())
                );
    }

    @GET
    @Path("/{id}")
    public Uni<RestResponse<InvoiceResponse>> getSingle(Long id) {
        return InvoiceDTO.findById(id).onItem().ifNotNull().transformToUni(invoice ->
            Mutiny.fetch(((InvoiceDTO)invoice).tags).map(tags -> RestResponse.ok(new InvoiceResponse((InvoiceDTO) invoice)))
        );
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<InvoiceResponse>> create(InvoiceRequest invoiceRequest) {
        InvoiceDTO invoice = new InvoiceDTO(invoiceRequest);
        invoice.tags = invoiceRequest.tags.stream().map(tagName -> new InvoiceTagDTO(invoice, tagName)).toList();

        return Panache.withTransaction(invoice::persist).onItem()
                .transform(entityBase -> new InvoiceResponse((InvoiceDTO) entityBase))
                .map(persistedItem -> RestResponse.status(RestResponse.Status.CREATED, persistedItem));
    }

}
