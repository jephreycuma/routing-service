package za.co.hellobuddy.records.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReloadlyTokenResponse(
	    @JsonProperty("access_token") String accessToken,
	    @JsonProperty("scope") String scope,
	    @JsonProperty("expires_in") Long expiresIn,
	    @JsonProperty("token_type") String tokenType
	) {}
