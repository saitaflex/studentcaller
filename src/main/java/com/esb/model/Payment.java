package com.esb.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
@Schema(description = "Settlement of a reservation (one per reservation)")
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Generated id", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "reservation_id", unique = true)
	@Schema(description = "The paid reservation")
	private Reservation reservation;

	@Schema(description = "Date of payment", example = "2026-11-09")
	private LocalDate paymentDate;
	@Schema(description = "Amount paid = reservation totalAmount", example = "20.00")
	private BigDecimal amount;
	@Enumerated(EnumType.STRING)
	@Schema(description = "PENDING or PAID", example = "PAID")
	private PaymentStatus paymentStatus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Reservation getReservation() {
		return reservation;
	}

	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
}
