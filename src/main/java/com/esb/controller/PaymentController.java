package com.esb.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.esb.model.Payment;
import com.esb.model.PaymentStatus;
import com.esb.service.PaymentService;
import com.esb.service.ReservationService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
	@Autowired
	PaymentService paymentService;

	@Autowired
	ReservationService reservationService;

	@PostMapping("/{reservationId}")
	@ResponseStatus(HttpStatus.CREATED)
	public Payment doPayment(@PathVariable Long reservationId, @RequestParam BigDecimal amount) {
		return paymentService.doPayment(reservationService.findReservationById(reservationId), amount);
	}

	@GetMapping
	public List<Payment> listPayments() {
		return paymentService.listPayments();
	}

	@GetMapping("/{id}")
	public Payment getPayment(@PathVariable Long id) {
		return paymentService.findPaymentById(id);
	}

	@PutMapping("/{id}/status")
	public Payment updatePaymentStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
		return paymentService.updatePaymentStatus(id, status);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePayment(@PathVariable Long id) {
		paymentService.deletePayment(id);
	}
}
