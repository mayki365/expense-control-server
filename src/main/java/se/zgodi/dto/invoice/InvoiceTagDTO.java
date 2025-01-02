package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name="invoice_tag")
@Cacheable
public class InvoiceTagDTO extends PanacheEntity {

    public InvoiceTagDTO() {}
    public InvoiceTagDTO(InvoiceDTO invoice, String tag) {
        this.invoice = invoice;
        this.tag = tag;
    }


    @ManyToOne
    @JoinColumn(name = "invoice_id")
    public InvoiceDTO invoice;

    @Column(length = 50)
    public String tag;
}
