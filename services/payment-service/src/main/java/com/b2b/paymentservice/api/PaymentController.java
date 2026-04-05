package com.b2b.paymentservice.api;

import com.B2B.extra.StatusPayment;
import com.b2b.paymentservice.api.dto.PaymentRequest;
import com.b2b.paymentservice.api.dto.PaymentResponse;
import com.b2b.paymentservice.app.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentController
{
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService)
    {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest)
    {
        return new ResponseEntity<>(paymentService.createPayment(paymentRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id)
    {
        return new ResponseEntity<>(paymentService.getPayment(id), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getPayments()
    {
        return new  ResponseEntity<>(paymentService.getPayments(), HttpStatus.OK);
    }

    @PostMapping("/{paymentId}/{status}")
    public void updatePayment(@PathVariable UUID paymentId,@PathVariable StatusPayment status)
    {
        paymentService.updatePayment(paymentId, status);
    }

}
