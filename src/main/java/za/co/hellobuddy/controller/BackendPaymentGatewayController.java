package za.co.hellobuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/gateway")
public class BackendPaymentGatewayController {

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> handleMerchantSettlement(@RequestBody Map<String, Object> paymentPayload) {
        
        System.out.println("Processing transaction for item: " + paymentPayload.get("name"));
        System.out.println("Withdrawing amount: R" + paymentPayload.get("price") + " via external bank node...");
        System.out.println("Settling funds directly into merchant credit account ledger.");

        // Formulate transaction response payload packet
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("status", "SUCCESS_SETTLED");
        receipt.put("reference", "VOD-TXT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        receipt.put("amountSettled", paymentPayload.get("price"));
        
        return ResponseEntity.ok(receipt);
    }
}