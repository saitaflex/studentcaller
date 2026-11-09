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

import com.esb.config.ApiDocs;
import com.esb.model.Payment;
import com.esb.model.PaymentStatus;
import com.esb.service.PaymentService;
import com.esb.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "7. Payments")
public class PaymentController {

	@Autowired
	PaymentService paymentService;

	@Autowired
	ReservationService reservationService;

	@Operation(summary = "Pay a reservation", description = """
			Settles a reservation. The `amount` must be **exactly** the reservation's `totalAmount`
			(see `GET /api/reservations/{id}`). A reservation is paid only once and a cancelled one cannot be paid.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Payment recorded with status PAID"),
			@ApiResponse(responseCode = "400", description = "Wrong amount, already paid or cancelled", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Wrong amount", value = "{\"error\": \"Incorrect Amount\"}"),
							@ExampleObject(name = "Already paid", value = "{\"error\": \"Reservation is already paid\"}"),
							@ExampleObject(name = "Cancelled", value = "{\"error\": \"Cannot pay a cancelled reservation\"}") })),
			@ApiResponse(responseCode = "404", description = "Reservation not found",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.RESERVATION_NOT_FOUND))) })
	@PostMapping("/{reservationId}")
	@ResponseStatus(HttpStatus.CREATED)
	public Payment doPayment(@Parameter(description = "Reservation to pay", example = "1") @PathVariable Long reservationId,
			@Parameter(description = "Amount paid (must equal the reservation total)", example = "20") @RequestParam BigDecimal amount) {
		return paymentService.doPayment(reservationService.findReservationById(reservationId), amount);
	}

	@Operation(summary = "List all payments", description = "Each payment includes the full reservation it settles.")
	@GetMapping
	public List<Payment> listPayments() {
		return paymentService.listPayments();
	}

	@Operation(summary = "Get one payment")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Payment found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.PAYMENT_NOT_FOUND))) })
	@GetMapping("/{id}")
	public Payment getPayment(@Parameter(description = "Payment id", example = "1") @PathVariable Long id) {
		return paymentService.findPaymentById(id);
	}

	@Operation(summary = "Change a payment status", description = "A `PAID` payment cannot be set back to `PENDING`.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Status updated"),
			@ApiResponse(responseCode = "400", description = "PAID → PENDING refused", content = @Content(mediaType = ApiDocs.JSON,
					examples = @ExampleObject(value = "{\"error\": \"A paid payment cannot be set back to pending\"}"))),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.PAYMENT_NOT_FOUND))) })
	@PutMapping("/{id}/status")
	public Payment updatePaymentStatus(@Parameter(description = "Payment id", example = "1") @PathVariable Long id,
			@Parameter(description = "New status", example = "PAID") @RequestParam PaymentStatus status) {
		return paymentService.updatePaymentStatus(id, status);
	}

	@Operation(summary = "Delete a payment")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Payment deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.PAYMENT_NOT_FOUND))) })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePayment(@Parameter(description = "Payment id", example = "1") @PathVariable Long id) {
		paymentService.deletePayment(id);
	}
}
