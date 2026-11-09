package com.esb.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Schema(description = "A classroom, lab or hall that can be reserved")
public class Room {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Generated id", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank(message = "Room name is required")
	@Schema(description = "Room name", example = "A101")
	private String name;

	@Min(value = 1, message = "Capacity must be at least 1")
	@Schema(description = "Number of seats (at least 1)", example = "30")
	private int capacity;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Enumerated(EnumType.STRING)
	@Schema(description = "AVAILABLE or RESERVED - managed automatically by reservations", example = "AVAILABLE", accessMode = Schema.AccessMode.READ_ONLY)
	private RoomStatus status;

	@NotNull(message = "Price per hour is required")
	@PositiveOrZero(message = "Price per hour must be positive")
	@Schema(description = "Price of one hour", example = "10.00")
	private BigDecimal pricePerHour;

	@NotNull(message = "Building is required")
	@ManyToOne
	@JoinColumn(name = "building_id")
	@Schema(description = "Building of the room (only its id is needed when sending)")
	private Building building;

	// Owning side of the Many-to-Many: Hibernate fills the join table room_equipment
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@ManyToMany
	@JoinTable(name = "room_equipment", joinColumns = @JoinColumn(name = "room_id"),
			inverseJoinColumns = @JoinColumn(name = "equipment_id"))
	@Schema(description = "Equipment installed in the room - managed with PUT/DELETE /api/rooms/{roomId}/equipments/{equipmentId}",
			accessMode = Schema.AccessMode.READ_ONLY)
	private Set<Equipment> equipments = new HashSet<>();

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

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}

	public BigDecimal getPricePerHour() {
		return pricePerHour;
	}

	public void setPricePerHour(BigDecimal pricePerHour) {
		this.pricePerHour = pricePerHour;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public Set<Equipment> getEquipments() {
		return equipments;
	}

	public void setEquipments(Set<Equipment> equipments) {
		this.equipments = equipments;
	}
}
