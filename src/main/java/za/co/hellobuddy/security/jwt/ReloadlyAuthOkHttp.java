package za.co.hellobuddy.security.jwt;

import javax.net.ssl.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import za.co.hellobuddy.records.security.ReloadlyTokenResponse;

import java.security.cert.CertificateException;

@Service
public class ReloadlyAuthOkHttp {
	
	 @Value("${reloadly.client-id}")
	    private String clientId;

	    @Value("${reloadly.client-secret}")
	    private String clientSecret;

	    private static final String GRANT_TYPE = "client_credentials";
	    
    public ReloadlyTokenResponse getAccessToken(String audience) throws Exception {
       
    	TokenParser tokenParser = new TokenParser();
        // 1. Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
        };

        // 2. Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // 3. Create an OkHttpClient that uses the modified SSL configuration
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true) // Trust all hostnames
                .build();

        // 4. Your standard request payload setup
        MediaType mediaType = MediaType.parse("application/json");
        String jsonBody = tokenParser.buildJsonBody(clientId,clientSecret,GRANT_TYPE,audience);
       
        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request = new Request.Builder()
                .url("https://auth.reloadly.com/oauth/token")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // 5. Execute the call
        try (Response response = client.newCall(request).execute()) {
            
            return tokenParser.parseTokenResponse(response);
        }
    }
}