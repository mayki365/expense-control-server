package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.util.List;


@Entity
@Table(name="transaction")
@Cacheable
public class TransactionDTO extends PanacheEntity {

    public TransactionDTO() {}
    public TransactionDTO(TransactionRequest transactionRequest) {
        this.name = transactionRequest.name;
        this.description = transactionRequest.description;
        this.amount = transactionRequest.amount;
    }

    @Column(length = 50)
    public String name;

    @Column(length = 250)
    public String description;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    public List<TransactionTagDTO> tags;

    @Column(precision = 10, scale = 2)
    public BigDecimal amount;

}
