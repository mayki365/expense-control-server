package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transaction_items")
public class TransactionItemDTO extends PanacheEntity {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id")
    public TransactionDTO transaction;

    @Column(length = 100)
    public String name;

    @Column(length = 500)
    public String description;

    @Column(precision = 10, scale = 2)
    public BigDecimal amount;

    @Column(precision = 10, scale = 3)
    public BigDecimal quantity;

    @Column(length = 20)
    public String quantityUnit;

    @Column(name = "unit_price", precision = 10, scale = 2)
    public BigDecimal unitPrice;

    @Column(length = 50)
    public String category;

    public TransactionItemDTO() {}

    public TransactionItemDTO(TransactionItemRequest request) {
        this.name = request.name;
        this.description = request.description;
        this.amount = request.amount;
        this.category = request.category;
        this.quantity = request.quantity;
        this.quantityUnit = request.quantityUnit;
        this.unitPrice = request.unitPrice;
    }
}
