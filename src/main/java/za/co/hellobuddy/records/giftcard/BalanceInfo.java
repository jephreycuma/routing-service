package za.co.hellobuddy.records.giftcard;

import java.math.BigDecimal;

public record BalanceInfo(
    BigDecimal oldBalance,
    BigDecimal newBalance,
    BigDecimal cost,
    String currencyCode,
    String currencyName,
    String updatedAt
) {}