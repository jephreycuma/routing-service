package za.co.hellobuddy.records.giftcard;

import java.math.BigDecimal;

public record ProductInfo(
    long productId,
    String productName,
    String countryCode,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    String currencyCode,
    BrandInfo brand
) {}
