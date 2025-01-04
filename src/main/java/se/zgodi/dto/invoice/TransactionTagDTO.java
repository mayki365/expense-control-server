package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name="transaction_tag")
@Cacheable
public class TransactionTagDTO extends PanacheEntity {

    public TransactionTagDTO() {}
    public TransactionTagDTO(TransactionDTO transaction, String tag) {
        this.transaction = transaction;
        this.tag = tag;
    }


    @ManyToOne
    @JoinColumn(name = "transaction_id")
    public TransactionDTO transaction;

    @Column(length = 50)
    public String tag;
}
