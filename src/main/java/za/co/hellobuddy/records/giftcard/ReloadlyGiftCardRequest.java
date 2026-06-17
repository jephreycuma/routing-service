package za.co.hellobuddy.records.giftcard;

public record ReloadlyGiftCardRequest(
	    int productId,
	    String countryCode,
	    int quantity,
	    double unitPrice,
	    String customIdentifier,
	    String senderName,
	    String recipientEmail,
	    RecipientPhoneDetails recipientPhoneDetails
	) {}
 
