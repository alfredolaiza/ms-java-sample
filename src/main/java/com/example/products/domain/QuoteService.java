package com.example.products.domain;

import com.example.products.entities.Product;
import com.example.products.entities.Quote;
import com.example.products.entities.QuoteItem;
import com.example.products.repositories.ProductRepository;
import com.example.products.repositories.QuoteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuoteService {

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    private final QuoteRepository quoteRepository;
    private final ProductRepository productRepository;

    public QuoteService(QuoteRepository quoteRepository, ProductRepository productRepository) {
        this.quoteRepository = quoteRepository;
        this.productRepository = productRepository;
    }

    public Quote createQuote(CreateQuoteRequest request) {
        Quote quote = new Quote();
        quote.setCustomerName(request.getCustomerName());
        quote.setCustomerEmail(request.getCustomerEmail());
        quote.setCustomerDocument(request.getCustomerDocument());
        quote.setQuoteDate(Instant.now());

        boolean pricesIncludeTax = Boolean.TRUE.equals(request.getPricesIncludeTax());
        quote.setPricesIncludeTax(pricesIncludeTax);
        quote.setTaxRate(IGV_RATE);

        List<QuoteItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (CreateQuoteItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.getProductId()));

            if (Boolean.FALSE.equals(product.getActive())) {
                throw new IllegalStateException("Product is inactive: " + product.getId());
            }

            BigDecimal quantity = itemReq.getQuantity().setScale(2, RoundingMode.HALF_UP);
            BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);

            BigDecimal lineSubtotal;
            BigDecimal lineTax;
            BigDecimal lineTotal;

            if (!pricesIncludeTax) {
                lineSubtotal = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                lineTax = lineSubtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
                lineTotal = lineSubtotal.add(lineTax).setScale(2, RoundingMode.HALF_UP);
            } else {
                BigDecimal baseTotal = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                lineSubtotal = baseTotal.divide(BigDecimal.ONE.add(IGV_RATE), 2, RoundingMode.HALF_UP);
                lineTax = baseTotal.subtract(lineSubtotal).setScale(2, RoundingMode.HALF_UP);
                lineTotal = baseTotal;
            }

            QuoteItem item = new QuoteItem();
            item.setQuote(quote);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            if (product.getCategory() != null) {
                item.setCategoryName(product.getCategory().getName());
            }
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setLineSubtotal(lineSubtotal);
            item.setLineTaxAmount(lineTax);
            item.setLineTotal(lineTotal);

            items.add(item);

            subtotal = subtotal.add(lineSubtotal);
            taxTotal = taxTotal.add(lineTax);
            grandTotal = grandTotal.add(lineTotal);
        }

        quote.setItems(items);
        quote.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        quote.setTaxAmount(taxTotal.setScale(2, RoundingMode.HALF_UP));
        quote.setTotalAmount(grandTotal.setScale(2, RoundingMode.HALF_UP));

        return quoteRepository.save(quote);
    }

    public Optional<Quote> getQuoteById(Long id) {
        return quoteRepository.findById(id);
    }

    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    // DTOs internos para el servicio (pueden extraerse a un package dto si se prefiere)
    public static class CreateQuoteRequest {
        private String customerName;
        private String customerEmail;
        private String customerDocument;
        private Boolean pricesIncludeTax;
        private List<CreateQuoteItemRequest> items;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public String getCustomerDocument() { return customerDocument; }
        public void setCustomerDocument(String customerDocument) { this.customerDocument = customerDocument; }
        public Boolean getPricesIncludeTax() { return pricesIncludeTax; }
        public void setPricesIncludeTax(Boolean pricesIncludeTax) { this.pricesIncludeTax = pricesIncludeTax; }
        public List<CreateQuoteItemRequest> getItems() { return items; }
        public void setItems(List<CreateQuoteItemRequest> items) { this.items = items; }
    }

    public static class CreateQuoteItemRequest {
        private Long productId;
        private BigDecimal quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    }
}

