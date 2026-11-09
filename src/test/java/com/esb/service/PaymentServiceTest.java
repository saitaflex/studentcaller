package com.esb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esb.model.Payment;
import com.esb.model.PaymentStatus;
import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;
import com.esb.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	PaymentRepository paymentRepo;

	@InjectMocks
	PaymentService service;

	Reservation reservation;

	@BeforeEach
	void setUp() {
		reservation = new Reservation();
		reservation.setId(7L);
		reservation.setReservationStatus(ReservationStatus.PENDING);
		reservation.setTotalAmount(new BigDecimal("30.00"));
	}

	@Test
	void doPayment_succeedsWithExactAmount() {
		when(paymentRepo.existsByReservationId(7L)).thenReturn(false);
		when(paymentRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		Payment p = service.doPayment(reservation, new BigDecimal("30"));

		assertEquals(PaymentStatus.PAID, p.getPaymentStatus());
	}

	@Test
	void doPayment_rejectsSecondPayment() {
		when(paymentRepo.existsByReservationId(7L)).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> service.doPayment(reservation, new BigDecimal("30.00")));
		verify(paymentRepo, never()).save(any());
	}

	@Test
	void doPayment_rejectsWrongAmount() {
		when(paymentRepo.existsByReservationId(7L)).thenReturn(false);

		assertThrows(IllegalStateException.class, () -> service.doPayment(reservation, new BigDecimal("25.00")));
	}

	@Test
	void doPayment_rejectsCancelledReservation() {
		reservation.setReservationStatus(ReservationStatus.CANCELLED);

		assertThrows(IllegalStateException.class, () -> service.doPayment(reservation, new BigDecimal("30.00")));
	}

	@Test
	void updatePaymentStatus_cannotRevertPaidToPending() {
		Payment paid = new Payment();
		paid.setPaymentStatus(PaymentStatus.PAID);
		when(paymentRepo.findById(1L)).thenReturn(Optional.of(paid));

		assertThrows(IllegalStateException.class, () -> service.updatePaymentStatus(1L, PaymentStatus.PENDING));
	}
}
