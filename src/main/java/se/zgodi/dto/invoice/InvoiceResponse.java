package se.zgodi.dto.invoice;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InvoiceResponse {
    public Long id;
    public String name;
    public String description;
    public List<String> tags;

    public InvoiceResponse(InvoiceDTO invoice) {
        this.id = invoice.id;
        this.name = invoice.name;
        this.description = invoice.description;
        if (invoice.tags != null && !invoice.tags.isEmpty()) {
            this.tags = invoice.tags.stream().map(tag -> tag.tag).toList();
        } else {
            this.tags = Collections.emptyList();
        }
    }
}
