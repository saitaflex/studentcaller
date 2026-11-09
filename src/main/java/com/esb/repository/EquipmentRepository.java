package com.esb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

	// Equipment name must be unique
	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
