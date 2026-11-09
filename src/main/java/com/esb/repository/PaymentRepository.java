package com.esb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	// Custom query 5: a reservation can only be paid once
	boolean existsByReservationId(Long reservationId);
}
