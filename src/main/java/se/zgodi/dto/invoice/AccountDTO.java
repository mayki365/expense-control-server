package se.zgodi.dto.invoice;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import se.zgodi.enums.AccountStatus;
import se.zgodi.enums.AccountType;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="account")
@Cacheable
public class AccountDTO extends PanacheEntity {
    @Column(length = 50)
    public String name;

    @Column(length = 200)
    public String description;

    @Enumerated(EnumType.STRING)
    public AccountType type;

    @Enumerated(EnumType.STRING)
    public AccountStatus status;

    @Column(precision = 10, scale = 2)
    public BigDecimal balance;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    public List<TransactionDTO> tranactions;

    public AccountDTO() { }
    public AccountDTO(AccountRequest accountRequest) {
        this.name = accountRequest.name;
        this.description = accountRequest.description;
        this.type = accountRequest.type;
        this.status = accountRequest.status;
        this.balance = accountRequest.balance;
    }

}
