package za.co.hellobuddy.records.topup;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public record Operator(
	    Long id,
	    Long operatorId,
	    String name,
	    boolean bundle,
	    boolean data,
	    boolean pin,
	    boolean comboProduct,
	    boolean supportsLocalAmounts,
	    boolean supportsGeographicalRechargePlans,
	    String denominationType,
	    String senderCurrencyCode,
	    String senderCurrencySymbol,
	    String destinationCurrencyCode,
	    String destinationCurrencySymbol,
	    BigDecimal commission,
	    BigDecimal internationalDiscount,
	    BigDecimal localDiscount,
	    BigDecimal mostPopularAmount,
	    BigDecimal mostPopularLocalAmount,
	    BigDecimal minAmount,
	    BigDecimal maxAmount,
	    BigDecimal localMinAmount,
	    BigDecimal localMaxAmount,
	    Country country,
	    Fx fx,
	    List<String> logoUrls,
	    List<BigDecimal> fixedAmounts,
	    Map<String, String> fixedAmountsDescriptions,
	    List<BigDecimal> localFixedAmounts,
	    Map<String, String> localFixedAmountsDescriptions,
	    List<BigDecimal> suggestedAmounts,
	    Map<String, BigDecimal> suggestedAmountsMap,
	    Fees fees,
	    List<String> geographicalRechargePlans,
	    List<Promotion> promotions,
	    String status
	) {}

	record Country(
	    String isoName,
	    String name
	) {}

	record Fx(
	    BigDecimal rate,
	    String currencyCode
	) {}

	record Fees(
	    BigDecimal international,
	    BigDecimal local,
	    BigDecimal localPercentage,
	    BigDecimal internationalPercentage
	) {}
	@JsonIgnoreProperties(ignoreUnknown = true)
	record Promotion(
	    Long id,
	    Long operatorId,
	    String title,
	    String description,
	    String validity,
	    String startDate,
	    String endDate
	) {}
