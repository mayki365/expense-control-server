package se.zgodi.dto.invoice;

import java.math.BigDecimal;

public class TransactionItemRequest {
    public String name;
    public String description;
    public BigDecimal amount;
    public String category;
    public BigDecimal quantity;
    public String quantityUnit;
    public BigDecimal unitPrice;
}
