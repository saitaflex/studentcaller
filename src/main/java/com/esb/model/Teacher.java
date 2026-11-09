package com.esb.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Schema(description = "A teacher who reserves rooms")
public class Teacher {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Generated id", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank(message = "Name is required")
	@Schema(description = "Full name", example = "Sami Ben Ali")
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Email is not valid")
	@Column(unique = true)
	@Schema(description = "Unique email address", example = "sami.benali@esb.tn")
	private String email;

	@NotNull(message = "Department is required")
	@ManyToOne
	@JoinColumn(name = "department_id")
	@Schema(description = "Department of the teacher (only its id is needed when sending)")
	private Department department;

	@JsonIgnore
	@OneToMany(mappedBy = "teacher")
	private List<Reservation> reservations;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public List<Reservation> getReservations() {
		return reservations;
	}

	public void setReservations(List<Reservation> reservations) {
		this.reservations = reservations;
	}
}
