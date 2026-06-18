package za.co.hellobuddy.reloadly.topup;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.hellobuddy.records.topup.TopupRequest;
import za.co.hellobuddy.reloadly.dto.TopupResponse;
import za.co.hellobuddy.security.jwt.ReloadlyAuthOkHttp;

import java.util.UUID;

@Service
public class ReloadlyTopup {
	
    @Value("${reloadly.audience}")
    private String audience;
    private static final String TOPUP_ENDPOINT = "/topups";

	@Autowired
	ReloadlyAuthOkHttp reloadlyAuthOkHttp;
	
    public TopupResponse doTopup(double amount, Long senderPhone, Long recipientPhone, String countrISOName, int operatorId, String emailAddress, boolean useLocalAmount) throws Exception {
        // 1. Define the target URL and Token
    	TopupResponse topupResponse = null;
        String url = audience+TOPUP_ENDPOINT;
        String token = reloadlyAuthOkHttp.getAccessToken(audience).accessToken();
        
        HttpClient client = HttpClient.newHttpClient();
        
        ObjectMapper mapper = new ObjectMapper();        

        // 1. Instantiate your records easily using the canonical constructor
        TopupRequest.PhoneInfo recipient = new TopupRequest.PhoneInfo(countrISOName, recipientPhone);
        TopupRequest.PhoneInfo sender = null;
        if(senderPhone != null && senderPhone > 0) {
			sender = new TopupRequest.PhoneInfo(countrISOName, senderPhone);
		} 
        new TopupRequest.PhoneInfo(countrISOName, senderPhone);
        String systemUniqueRef = "HB-" + UUID.randomUUID().toString();

        TopupRequest requestPayload = new TopupRequest(
        	operatorId, 
        	amount, // Switched to a safe test amount for your sandbox!
        	useLocalAmount, 
            systemUniqueRef, 
            emailAddress, 
            recipient, 
            sender
        );

        // 2. Convert record to JSON String
        String jsonString = mapper.writeValueAsString(requestPayload);
        System.out.println(jsonString);

        // 3. Create the HTTP Client
        

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        try {
            // 2. Send the Request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                // 3. Initialize Jackson ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();

                // 4. Map the JSON response string directly into your TopupResponse object
                topupResponse = objectMapper.readValue(response.body(), TopupResponse.class);

                // 5. Access data safely using the object's strongly-typed getters
                System.out.println("--- Topup Transaction Successful ---");
                System.out.println("Transaction ID: " + topupResponse.getTransactionId());
                System.out.println("Status: " + topupResponse.getStatus());
                System.out.println("Operator: " + topupResponse.getOperatorName());
                //System.out.println("Requested Amount: " + topupResponse.getRequestedAmount());
                System.out.println("Delivered Amount: " + topupResponse.getDeliveredAmount() + " " + topupResponse.getDeliveredAmountCurrencyCode());
                
                // Accessing nested object fields
                if (topupResponse.getBalanceInfo() != null) {
                    System.out.println("New Wallet Balance: " + topupResponse.getBalanceInfo().getNewBalance());
                    
                    //return topupResponse;
                }
                
            } else {
                System.err.println("API Request Failed with Status Code: " + response.statusCode());
                System.err.println("Error Body: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred during execution: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topupResponse;
    }
}
