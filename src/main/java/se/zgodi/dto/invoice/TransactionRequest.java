package se.zgodi.dto.invoice;

import java.math.BigDecimal;
import java.util.List;

public class TransactionRequest {
    public Long id;
    public String name;
    public String description;
    public List<String> tags;
    public BigDecimal amount;
}
