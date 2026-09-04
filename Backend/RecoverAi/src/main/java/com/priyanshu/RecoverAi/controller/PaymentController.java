package com.priyanshu.RecoverAi.controller;

import java.util.List;

import com.priyanshu.RecoverAi.DTO.PaymentCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.priyanshu.RecoverAi.DTO.PaymentResponse;
import com.priyanshu.RecoverAi.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/get-all")
    public List<PaymentResponse> getPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/failed")
    public List<PaymentResponse> getFailedPayments() {
        return paymentService.getFailedPayments();
    }

    @PostMapping("/create-payment")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody PaymentCreateRequest request){
        return paymentService.createPayment(request);
    }


}
