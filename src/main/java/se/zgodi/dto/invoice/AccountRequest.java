package se.zgodi.dto.invoice;

import se.zgodi.enums.AccountStatus;
import se.zgodi.enums.AccountType;

import java.math.BigDecimal;

public class AccountRequest  {
    public Long id;
    public String name;
    public String description;
    public AccountType type;
    public AccountStatus status;
    public BigDecimal balance;
}
