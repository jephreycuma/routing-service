package za.co.hellobuddy.records.giftcard;

import java.math.BigDecimal;

public record ReloadlyGiftCardResponse(
    long transactionId,
    BigDecimal amount,
    BigDecimal discount,
    String currencyCode,
    BigDecimal fee,
    BigDecimal smsFee,
    BigDecimal totalFee,
    boolean preOrdered,
    String recipientEmail,
    String recipientPhone,
    String customIdentifier,
    String status,
    String transactionCreatedTime, // You can change this to LocalDateTime if using a custom Jackson deserializer
    ProductInfo product,
    BalanceInfo balanceInfo
) {}
