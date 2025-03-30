package se.zgodi.dto.invoice;

import java.math.BigDecimal;

public class TransactionItemResponse {
    public Long id;
    public String name;
    public String description;
    public BigDecimal amount;
    public String category;
    public BigDecimal quantity;
    public String quantityUnit;
    public BigDecimal unitPrice;

    public TransactionItemResponse(TransactionItemDTO item) {
        this.id = item.id;
        this.name = item.name;
        this.description = item.description;
        this.amount = item.amount;
        this.category = item.category;
        this.quantity = item.quantity;
        this.quantityUnit = item.quantityUnit;
        this.unitPrice = item.unitPrice;
    }
}
