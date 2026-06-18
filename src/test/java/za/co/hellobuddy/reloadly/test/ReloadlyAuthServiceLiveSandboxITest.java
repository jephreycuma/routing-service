package za.co.hellobuddy.reloadly.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.hellobuddy.records.giftcard.ReloadlyGiftCardResponse;
import za.co.hellobuddy.records.security.ReloadlyTokenResponse;
import za.co.hellobuddy.reloadly.giftcard.ReloadlyGiftCardOrder;
import za.co.hellobuddy.reloadly.topup.ReloadlyTopup;
import za.co.hellobuddy.reloadly.topup.RetrieveOperators;
import za.co.hellobuddy.security.jwt.ReloadlyAuthOkHttp;

@SpringBootTest
@ActiveProfiles("dev")
class ReloadlyAuthServiceLiveSandboxITest {

	@Autowired
	private ReloadlyAuthOkHttp reloadlyAuthOkHttp;

	@Autowired
	private ReloadlyTopup reloadlyTopup;

	@Autowired
	private RetrieveOperators retrieveOperators;

	@Autowired
	private ReloadlyGiftCardOrder reloadlyGiftCardOrder;

	@Test
	void getAccessToken_ShouldReturnValidToken_FromLiveSandbox() throws Exception {
		// When
		ReloadlyTokenResponse response = reloadlyAuthOkHttp.getAccessToken("https://topups-sandbox.reloadly.com");

		// Then
		assertNotNull(response, "The response record should not be null");

		// Using direct record component accessors (no 'get' prefix)
		// Swap to .access_token() if the record field uses snake_case naming
		assertNotNull(response.accessToken(), "Access token should not be null");
		assertFalse(response.accessToken().isBlank(), "Access token should not be empty");

		System.out.println("Successfully retrieved live Sandbox Token: " + response.accessToken());
	}

	@Test
	void doTopup_ShouldSucceed_WithValidToken() throws Exception {

		// When - Attempt a top-up using the valid token
		try {
			reloadlyTopup.doTopup(2, 0L, 27728793170L, "ZA", 441, "",
					true);
			System.out.println("Top-up request sent successfully to Reloadly Sandbox.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Top-up failed: " + e.getMessage());
		}
	}

	@Test
	void getOperators_ShouldReturnOperators_ForValidCountry() throws Exception {
		// Given
		String countryISOName = "ZA"; // Nigeria - Ensure this is a valid country code in your Reloadly Sandbox
										// account

		// When
		try {
			retrieveOperators.getOperators(countryISOName);
			System.out.println("Successfully retrieved operators for country: " + countryISOName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to retrieve operators: " + e.getMessage());
		}
	}

	@Test
	void getGiftCardOrder_ShouldSucceed_WithValidToken() throws Exception {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ReloadlyGiftCardResponse reloadlyGiftCardResponse = reloadlyGiftCardOrder.orderGiftCard(10, "ZA", 5, 5,
					"John Doe", "jephreycuma@hellobuddy.co.za", "27728703170");
			String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(reloadlyGiftCardResponse);

// 3. Print the formatted JSON structure
			System.out.println("Gift Card Order Response (JSON):\n" + jsonResponse);

			System.out.println("Gift card order request sent successfully to Reloadly Sandbox.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Gift card order failed: " + e.getMessage());
		}
	}
}