package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name="account_transaction")
@Cacheable
public class TransactionDTO extends PanacheEntity {

    public TransactionDTO() {}
    public TransactionDTO(TransactionRequest transactionRequest) {
        this.name = transactionRequest.name;
        this.description = transactionRequest.description;
        this.amount = transactionRequest.amount;
        this.eventDate = transactionRequest.eventDate;
        this.bankStatementDate = transactionRequest.bankStatementDate;
    }

    @Column(length = 50)
    public String name;

    @Column(length = 250)
    public String description;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    public List<TransactionTagDTO> tags;

    @ManyToOne
    @JoinColumn(name = "account_id")
    public AccountDTO account;

    @Column(precision = 10, scale = 2)
    public BigDecimal amount;

    @Column(name = "event_date", nullable = true)
    public LocalDateTime eventDate;  // Date when the transaction actually happened

    @Column(name = "bank_statement_date", nullable = true)
    public LocalDate bankStatementDate;  // Date when the bank processed the transaction

}
