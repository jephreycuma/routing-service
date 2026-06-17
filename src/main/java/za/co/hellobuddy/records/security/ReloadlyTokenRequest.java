package za.co.hellobuddy.records.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReloadlyTokenRequest(
	    @JsonProperty("client_id") String clientId,
	    @JsonProperty("client_secret") String clientSecret,
	    @JsonProperty("grant_type") String grantType,
	    @JsonProperty("audience") String audience
	) {}
