package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;


@Entity
@Table(name="invoice")
@Cacheable
public class InvoiceDTO extends PanacheEntity {

    public InvoiceDTO() {}
    public InvoiceDTO(InvoiceRequest invoiceRequest) {
        this.name = invoiceRequest.name;
        this.description = invoiceRequest.description;
    }

    @Column(length = 50)
    public String name;

    @Column(length = 250)
    public String description;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    public List<InvoiceTagDTO> tags;

}
