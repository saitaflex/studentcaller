package com.esb.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.model.Equipment;
import com.esb.repository.EquipmentRepository;

@Service
public class EquipmentService {

	@Autowired
	EquipmentRepository equipmentRepo;

	public Equipment addEquipment(Equipment equipment) {
		if (equipmentRepo.existsByNameIgnoreCase(equipment.getName())) {
			throw new IllegalStateException("Equipment name already exists");
		}
		return equipmentRepo.save(equipment);
	}

	public List<Equipment> listEquipments() {
		return equipmentRepo.findAll();
	}

	public Equipment findEquipmentById(Long id) {
		return equipmentRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Equipment not found"));
	}

	public Equipment updateEquipment(Long id, Equipment newData) {
		Equipment equipment = findEquipmentById(id);
		if (equipmentRepo.existsByNameIgnoreCaseAndIdNot(newData.getName(), id)) {
			throw new IllegalStateException("Equipment name already exists");
		}
		equipment.setName(newData.getName());
		equipment.setDescription(newData.getDescription());
		return equipmentRepo.save(equipment);
	}

	public void deleteEquipment(Long id) {
		equipmentRepo.delete(findEquipmentById(id));
	}
}
