package school.faang.promotionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import school.faang.promotionservice.config.client.FeignConfig;
import school.faang.promotionservice.dto.payment.PaymentRequest;
import school.faang.promotionservice.dto.payment.PaymentResponse;

@FeignClient(
        name = "payment-service",
        url = "${payment-service.service.url",
        configuration = FeignConfig.class
)
public interface PaymentClient {

    @PostMapping("/api/v1/payment")
    ResponseEntity<PaymentResponse> sendPayment(@RequestBody @Validated PaymentRequest dto);
}
