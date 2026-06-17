package za.co.hellobuddy.records.topup;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TopupRequest(
    int operatorId,
    double amount,
    boolean useLocalAmount,
    String customIdentifier,
    String recipientEmail,
    PhoneInfo recipientPhone,
    PhoneInfo senderPhone
) {
    // Nested record helper for phone payloads
    public record PhoneInfo(
        String countryCode,
        long number
    ) {}
}
