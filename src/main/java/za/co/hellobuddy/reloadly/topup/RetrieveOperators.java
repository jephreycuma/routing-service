package za.co.hellobuddy.reloadly.topup;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import za.co.hellobuddy.records.topup.Operator;
import za.co.hellobuddy.security.jwt.ReloadlyAuthOkHttp;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@Service
public class RetrieveOperators {
	
	 @Value("${reloadly.audience}")
	 private String audience;

	 private static final String OPERATORS_ENDPOINT = "/operators/countries/";
	
	@Autowired
	ReloadlyAuthOkHttp reloadlyAuthOkHttp;

    public List<Operator> getOperators(String countryISOName) throws Exception {
    	List<Operator> operators = null;
    	var token = reloadlyAuthOkHttp.getAccessToken(audience).accessToken();
    	
    	ObjectMapper objectMapper = new ObjectMapper();
        
        var url = audience+OPERATORS_ENDPOINT+countryISOName;
        
        try {
            // 1. Configure SSL context to trust all certificates
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                    .build();

            // 2. Build the HttpClient with the correct method: setTlsSocketStrategy
            try (CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                            .setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE))
                            .build())
                    .build()) {

                // 3. Prepare the GET request
                var request = new HttpGet(url);
                request.addHeader("Authorization", "Bearer " + token.stripIndent());
                request.addHeader("Accept", "*/*");

                System.out.println("Executing request to: " + request.getUri());

                // 4. Execute the request
                try (CloseableHttpResponse response = client.execute(request)) {
                    
                    System.out.print("""
                            
                            --- Response Info ---
                            Status Code: %d
                            
                            Headers:
                            """.formatted(response.getCode()));
                    
                    Arrays.stream(response.getHeaders())
                            .forEach(header -> System.out.printf("%s: %s%n", header.getName(), header.getValue()));

                    if (response.getEntity() != null) {
                        String result = EntityUtils.toString(response.getEntity());
                        operators = objectMapper.readValue(result, new TypeReference<List<Operator>>() {});
                        
                        // Now you can work with your strongly-typed records safely
                        System.out.printf("%nSuccessfully parsed %d operators.%n", operators.size());
                        
                        // Example: Print out the names of the parsed operators
                        operators.forEach(op -> System.out.printf("- %s (Status: %s)%n", op.name(), op.status()));
                        System.out.printf("%nResponse Body:%n%s%n", result);
                    }
                }
            }
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            System.err.println("SSL Configuration Error: " + e.getMessage());
        } catch (IOException | ParseException e) {
            System.err.println("HTTP Request Error: " + e.getMessage());
        }
        return operators;
    }
}