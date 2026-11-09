package com.esb.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Schema(description = "A room reserved by a teacher for a number of hours")
public class Reservation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Generated id", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "room_id")
	@Schema(description = "Reserved room")
	private Room room;

	@ManyToOne
	@JoinColumn(name = "teacher_id")
	@Schema(description = "Teacher who reserved")
	private Teacher teacher;

	@Schema(description = "Start / end of the reservation", example = "2026-11-09T10:00:00")
	private LocalDateTime startTime, endTime;
	@Enumerated(EnumType.STRING)
	@Schema(description = "PENDING (active), COMPLETED or CANCELLED", example = "PENDING")
	private ReservationStatus reservationStatus;
	@Schema(description = "pricePerHour x hours, updated on extension", example = "20.00")
	private BigDecimal totalAmount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public ReservationStatus getReservationStatus() {
		return reservationStatus;
	}

	public void setReservationStatus(ReservationStatus reservationStatus) {
		this.reservationStatus = reservationStatus;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
}
