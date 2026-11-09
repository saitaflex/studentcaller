package com.esb.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esb.model.Payment;
import com.esb.model.PaymentStatus;
import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;
import com.esb.repository.PaymentRepository;

@Service
public class PaymentService {

	@Autowired
	PaymentRepository paymentRepo;

	@Transactional
	public Payment doPayment(Reservation reservation, BigDecimal amount) {
		if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
			throw new IllegalStateException("Cannot pay a cancelled reservation");
		}
		if (paymentRepo.existsByReservationId(reservation.getId())) {
			throw new IllegalStateException("Reservation is already paid");
		}
		if (reservation.getTotalAmount().compareTo(amount) != 0) {
			throw new IllegalStateException("Incorrect Amount");
		}
		Payment payment = new Payment();
		payment.setReservation(reservation);
		payment.setPaymentDate(LocalDate.now());
		payment.setAmount(amount);
		payment.setPaymentStatus(PaymentStatus.PAID);
		return paymentRepo.save(payment);
	}

	public List<Payment> listPayments() {
		return paymentRepo.findAll();
	}

	public Payment findPaymentById(Long id) {
		return paymentRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Payment not found"));
	}

	public Payment updatePaymentStatus(Long id, PaymentStatus status) {
		Payment payment = findPaymentById(id);
		if (payment.getPaymentStatus() == PaymentStatus.PAID && status == PaymentStatus.PENDING) {
			throw new IllegalStateException("A paid payment cannot be set back to pending");
		}
		payment.setPaymentStatus(status);
		return paymentRepo.save(payment);
	}

	public void deletePayment(Long id) {
		paymentRepo.delete(findPaymentById(id));
	}
}
