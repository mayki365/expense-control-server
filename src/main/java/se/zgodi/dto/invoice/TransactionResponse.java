package se.zgodi.dto.invoice;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class TransactionResponse {
    public Long id;
    public Long accountId;
    public String name;
    public String description;
    public List<String> tags;
    public BigDecimal amount;

    public TransactionResponse(TransactionDTO invoice) {
        this.id = invoice.id;
        this.accountId = invoice.account != null ? invoice.account.id : null;
        this.name = invoice.name;
        this.description = invoice.description;
        this.amount = invoice.amount;
        if (invoice.tags != null && !invoice.tags.isEmpty()) {
            this.tags = invoice.tags.stream().map(tag -> tag.tag).toList();
        } else {
            this.tags = Collections.emptyList();
        }
    }
}
