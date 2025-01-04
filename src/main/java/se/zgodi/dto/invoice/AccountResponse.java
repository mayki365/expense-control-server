package se.zgodi.dto.invoice;

import se.zgodi.enums.AccountStatus;
import se.zgodi.enums.AccountType;

import java.math.BigDecimal;

public class AccountResponse {
    public Long id;
    public String name;
    public String description;
    public AccountType type;
    private AccountStatus status;
    public BigDecimal balance;

    public AccountResponse(AccountDTO entity) {
        this.id = entity.id;
        this.name = entity.name;
        this.description = entity.description;
        this.type = entity.type;
        this.status = entity.status;
        this.balance = entity.balance;
    }
}
