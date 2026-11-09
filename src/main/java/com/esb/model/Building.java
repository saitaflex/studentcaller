package com.esb.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

@Entity
@Schema(description = "A school building that contains rooms")
public class Building {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Generated id", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank(message = "Building name is required")
	@Schema(description = "Building name", example = "Bloc A")
	private String name;

	@Schema(description = "Address (optional)", example = "Campus Nord, Tunis")
	private String address;

	@JsonIgnore
	@OneToMany(mappedBy = "building")
	private List<Room> rooms;

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Room> getRooms() {
		return rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
}
