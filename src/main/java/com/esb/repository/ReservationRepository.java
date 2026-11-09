package com.esb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	// Custom query 3: reservations by status
	List<Reservation> findByReservationStatus(ReservationStatus reservationStatus);

	// Custom query 4: all reservations of a teacher
	List<Reservation> findByTeacherId(Long teacherId);
}
