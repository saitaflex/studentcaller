package com.esb.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.model.Building;
import com.esb.repository.BuildingRepository;

@Service
public class BuildingService {
	@Autowired
	BuildingRepository buildingRepo;

	public Building addBuilding(Building building) {
		return buildingRepo.save(building);
	}

	public List<Building> listBuildings() {
		return buildingRepo.findAll();
	}

	public Building findBuildingById(Long id) {
		return buildingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Building not found"));
	}

	public Building updateBuilding(Long id, Building newData) {
		Building building = findBuildingById(id);
		building.setName(newData.getName());
		building.setAddress(newData.getAddress());
		return buildingRepo.save(building);
	}

	public void deleteBuilding(Long id) {
		buildingRepo.delete(findBuildingById(id));
	}
}
