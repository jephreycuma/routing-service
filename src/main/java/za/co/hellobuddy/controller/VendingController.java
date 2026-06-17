package za.co.hellobuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.co.hellobuddy.model.Product;
import za.co.hellobuddy.records.topup.Operator;
import za.co.hellobuddy.reloadly.dto.TopupResponse;
import za.co.hellobuddy.reloadly.topup.ReloadlyTopup;
import za.co.hellobuddy.reloadly.topup.RetrieveOperators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telecom")
@CrossOrigin(origins = "http://localhost:3000") // Allow Node.js app to connect
public class VendingController {
	
	@Autowired
	private RetrieveOperators retrieveOperators;
	
	@Autowired
	private ReloadlyTopup reloadlyTopup;

    // Endpoint 1: Get Catalog with dynamic Country Filter
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(value = "country", required = false, defaultValue = "ZA") String country) throws Exception {
        
        List<Product> catalog = new ArrayList<>();
        
        // Dynamically pass the country parameter (e.g., "ZA" or "NG") downstream to Reloadly integration
        List<Operator> operators = retrieveOperators.getOperators(country);
        
        for(Operator operator : operators) {
            if(operator.pin()){               
                Map<String, String> amounts = operator.localFixedAmountsDescriptions();
                for (Map.Entry<String, String> entry : amounts.entrySet()) {
                    
                    Product product = new Product(
                        operator.id().toString(),
                        operator.name(),
                        "AIRTIME VOUCHER",
                        "PIN",
                        Double.parseDouble(entry.getKey().trim()), // Safely parse each unique amount key
                        entry.getValue(),                          // Get the matching descriptive text value
                        false,
                        operator.destinationCurrencySymbol(),
                        operator.destinationCurrencyCode()
                    );
                    
                    catalog.add(product);
                }
            } else if(operator.data()){               
                Map<String,String> amounts = operator.localFixedAmountsDescriptions();  
                for (Map.Entry<String, String> entry : amounts.entrySet()) {
                    
                    Product product = new Product(
                            operator.id().toString(),
                            operator.name(),
                            "DATA BUNDLES",
                            "PINLESS",
                            Double.parseDouble(entry.getKey().trim()), 
                            entry.getValue(),
                            false,
                            operator.destinationCurrencySymbol(),
                            operator.destinationCurrencyCode()
                    );
                    
                    catalog.add(product);
                }
                
            } else {
                Map<String,String> amounts = operator.localFixedAmountsDescriptions();
                if((amounts == null || amounts.isEmpty()) && operator.supportsLocalAmounts()) {
                    if(operator.localMinAmount() != null && operator.localMaxAmount() != null) {
                        Product product = new Product(
                                operator.id().toString(),
                                operator.name(),
                                "AIRTIME TOPUP",
                                "PINLESS",
                                operator.localMinAmount().doubleValue(), 
                                "Topup from "+operator.destinationCurrencySymbol() + operator.localMinAmount() + " to "+operator.destinationCurrencySymbol() + operator.localMaxAmount(),
                                true,
                                operator.destinationCurrencySymbol(),
                                operator.destinationCurrencyCode()); 
                        catalog.add(product);
                    }
                }
                
            }
        }
        return ResponseEntity.ok(catalog);
    }

    @PostMapping("/topups")
    public ResponseEntity<TopupResponse> topupProduct(@RequestParam("amount") double amount,
    		@RequestParam("senderPhoone") Long senderPhoone,
    		@RequestParam("receiverPhone") Long receiverPhone,
    		@RequestParam("countryISO") String countryISO,
    		@RequestParam("operatorId") Integer operatorId,
    		@RequestParam("senderEmail") String senderEmail,
    		@RequestParam("useLocalAmount") boolean useLocalAmount) throws Exception {

		TopupResponse topupResponse = reloadlyTopup.doTopup(amount, senderPhoone, receiverPhone, countryISO, operatorId, senderEmail,
				useLocalAmount);
		
		ResponseEntity<TopupResponse> response = ResponseEntity.ok(topupResponse);
		return response;
	}
    // Endpoint 2: Process Vending Request
    @PostMapping("/vend")
    public ResponseEntity<Map<String, Object>> vendProduct(@RequestBody Map<String, String> request) {
        List<Product> catalog = new ArrayList<>();
        String productId = request.get("productId");
        String phoneNumber = request.get("phoneNumber");

        // Find product
        Product product = catalog.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (product == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", "Product not found"));
        }

        // Simulating external third-party API call (e.g., Kazang/Blue Label)
        String reference = UUID.randomUUID().toString();
        
        if ("PINLESS".equals(product.getPinType())) {
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "reference", reference,
                "message", "Successfully topped up " + phoneNumber + " with " + product.getDescription()
            ));
        } else {
            // If it's a PIN, generate a fake recharge voucher pin
            String mockPin = String.format("%04d-%04d-%04d-%04d", 
                (int)(Math.random()*10000), (int)(Math.random()*10000), 
                (int)(Math.random()*10000), (int)(Math.random()*10000));
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "reference", reference,
                "pin", mockPin,
                "instructions", "Dial *130*111*PIN# to top up."
            ));
        }
    }
}