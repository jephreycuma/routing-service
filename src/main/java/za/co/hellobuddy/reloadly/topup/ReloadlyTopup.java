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
	
	public ReloadlyTopupResult doTopup(double amount, Long senderPhone, Long recipientPhone, String countrISOName, int operatorId, String emailAddress, boolean useLocalAmount) throws Exception {
	    String url = audience + TOPUP_ENDPOINT;
	    String token = reloadlyAuthOkHttp.getAccessToken(audience).accessToken();
	    
	    HttpClient client = HttpClient.newHttpClient();
	    ObjectMapper mapper = new ObjectMapper();        

	    TopupRequest.PhoneInfo recipient = new TopupRequest.PhoneInfo(countrISOName, recipientPhone);
	    TopupRequest.PhoneInfo sender = null;
	    if (senderPhone != null && senderPhone > 0) {
	        sender = new TopupRequest.PhoneInfo(countrISOName, senderPhone);
	    } 
	    
	    String systemUniqueRef = "HB-" + UUID.randomUUID().toString();

	    TopupRequest requestPayload = new TopupRequest(
	        operatorId, amount, useLocalAmount, systemUniqueRef, emailAddress, recipient, sender
	    );

	    String jsonString = mapper.writeValueAsString(requestPayload);
	    System.out.println(jsonString);

	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(url))
	            .header("Authorization", "Bearer " + token)
	            .header("Content-Type", "application/json")
	            .POST(HttpRequest.BodyPublishers.ofString(jsonString))
	            .build();

	    HttpResponse<String> response = null;
	    TopupResponse topupResponse = null;

	    try {
	        response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        if (response.statusCode() == 200 || response.statusCode() == 201) {
	            topupResponse = mapper.readValue(response.body(), TopupResponse.class);
	            
	            System.out.println("--- Topup Transaction Successful ---");
	            System.out.println("Transaction ID: " + topupResponse.getTransactionId());
	        } else {
	            System.err.println("API Request Failed with Status Code: " + response.statusCode());
	            System.err.println("Error Body: " + response.body());
	        }
	    } catch (IOException | InterruptedException e) {
	        System.err.println("An error occurred during execution: " + e.getMessage());
	        throw e; // Rethrow to let calling method know something structurally broke
	    }
	    
	    return new ReloadlyTopupResult(response, topupResponse);
	}
}
