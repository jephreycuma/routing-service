package za.co.hellobuddy.reloadly.giftcard;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.hellobuddy.records.giftcard.RecipientPhoneDetails;
import za.co.hellobuddy.records.giftcard.ReloadlyGiftCardRequest;
import za.co.hellobuddy.records.giftcard.ReloadlyGiftCardResponse; // Import your response record
import za.co.hellobuddy.security.jwt.ReloadlyAuthOkHttp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReloadlyGiftCardOrder {

    @Value("${reloadly.gift-card.audience}")
    private String audience;
    private static final String GIFT_CARD_ORDER_ENDPOINT = "/orders";
    
    @Autowired
    private ReloadlyAuthOkHttp reloadlyAuthOkHttp;

    // Best practice: Inject Jackson's pre-configured ObjectMapper provided by Spring
   // @Autowired
    //private ObjectMapper objectMapper;
    
    /**
     * Orders a gift card and maps the API response to a strongly-typed record.
     * @return ReloadlyGiftCardResponse or null if the request fails
     */
    public ReloadlyGiftCardResponse orderGiftCard(int productId,String countryCode,int quantity,double unitPrice,String senderName,String recipientEmail,String receiverPhone) throws Exception {
        String token = reloadlyAuthOkHttp.getAccessToken(audience).accessToken(); 
        String url = audience + GIFT_CARD_ORDER_ENDPOINT;
        ObjectMapper objectMapper = new ObjectMapper();
        String customIdentifier = "HB-" + UUID.randomUUID().toString();

        // 1. Instantiate the nested record objects
        RecipientPhoneDetails phoneDetails = new RecipientPhoneDetails(countryCode, receiverPhone);
        
        ReloadlyGiftCardRequest orderRequest = new ReloadlyGiftCardRequest(
        		productId,
        		countryCode,
        		quantity,
        		unitPrice,
                customIdentifier,
                senderName,
                recipientEmail,
                phoneDetails
        );

        try {
            // 2. Serialize the Record object into a JSON String using Jackson
            String jsonPayload = objectMapper.writeValueAsString(orderRequest);

            // 3. Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // 4. Build the HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // 5. Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());

            // 6. Map the JSON response string to your record if successful (HTTP 200/201 series)
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), ReloadlyGiftCardResponse.class);
            } else {
                System.err.println("API Error Response: " + response.body());
                // Handle non-2xx status codes here or throw a custom exception
                return null; 
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing request: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw e; // Rethrow or handle gracefully based on your architecture
        }
    }
}