package com.esb.controller;

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

import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;
import com.esb.service.ReservationService;
import com.esb.service.RoomService;
import com.esb.service.TeacherService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
	@Autowired
	private ReservationService reservationService;

	@Autowired
	private TeacherService teacherService;

	@Autowired
	private RoomService roomService;

	@PostMapping("/{teacherId}/{roomId}/{hours}")
	@ResponseStatus(HttpStatus.CREATED)
	public Reservation createReservation(@PathVariable Long teacherId, @PathVariable Long roomId,
			@PathVariable int hours) {
		return reservationService.createReservation(teacherService.findTeacherById(teacherId),
				roomService.findRoomById(roomId), hours);
	}

	@GetMapping
	public List<Reservation> listReservations() {
		return reservationService.listReservations();
	}

	@GetMapping("/{reservationId}")
	public Reservation getReservation(@PathVariable Long reservationId) {
		return reservationService.findReservationById(reservationId);
	}

	@PutMapping("/{reservationId}/complete")
	public Reservation completeReservation(@PathVariable Long reservationId) {
		return reservationService.completeReservation(reservationId);
	}

	@PutMapping("/{reservationId}/extend")
	public Reservation extendReservation(@PathVariable Long reservationId, @RequestParam int additionalHours) {
		return reservationService.extendReservation(reservationId, additionalHours);
	}

	@PutMapping("/{reservationId}/cancel")
	public Reservation cancelReservation(@PathVariable Long reservationId) {
		return reservationService.cancelReservation(reservationId);
	}

	@GetMapping("/status")
	public List<Reservation> obtainReservationsByStatus(@RequestParam ReservationStatus reservationStatus) {
		return reservationService.obtainReservationsByStatus(reservationStatus);
	}

	@GetMapping("/teacher/{teacherId}")
	public List<Reservation> obtainReservationsByTeacher(@PathVariable Long teacherId) {
		return reservationService.obtainReservationsByTeacher(teacherId);
	}

	@DeleteMapping("/{reservationId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteReservation(@PathVariable Long reservationId) {
		reservationService.deleteReservation(reservationId);
	}
}
