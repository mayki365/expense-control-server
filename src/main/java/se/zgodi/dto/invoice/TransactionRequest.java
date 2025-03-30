package se.zgodi.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionRequest {
    public Long id;
    public Long accountId;
    public String name;
    public String description;
    public List<String> tags;
    public BigDecimal amount;
    public LocalDateTime eventDate;
    public LocalDate bankStatementDate;
}
