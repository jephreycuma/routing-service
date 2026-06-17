package za.co.hellobuddy.security.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;
import okhttp3.ResponseBody;
import za.co.hellobuddy.records.security.ReloadlyTokenRequest;
import za.co.hellobuddy.records.security.ReloadlyTokenResponse;

import java.io.IOException;

public class TokenParser {

    // Reuse the ObjectMapper instance (it is thread-safe and expensive to create)
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ReloadlyTokenResponse parseTokenResponse(Response response) throws IOException {
        // 1. Ensure the HTTP request was successful
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected HTTP code: " + response);
        }

        // 2. Safely get the response body
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                throw new IOException("Response body is empty");
            }

            // 3. Deserialize directly from the response body byte stream
            return objectMapper.readValue(responseBody.byteStream(), ReloadlyTokenResponse.class);
        }
    }
    
    public String buildJsonBody(String clientId,String secretId, String grantType,String audiance) throws JsonProcessingException {
        // 1. Instantiate your record with the payload details
        ReloadlyTokenRequest request = new ReloadlyTokenRequest(
        		clientId,
        		secretId,
        		grantType,
        		audiance
        );

        // 2. Serialize the record object to a JSON String
        return objectMapper.writeValueAsString(request);
    }
}