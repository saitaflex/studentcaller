package com.esb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.esb.model.Building;
import com.esb.service.BuildingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {
	@Autowired
	BuildingService buildingService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Building addBuilding(@Valid @RequestBody Building building) {
		return buildingService.addBuilding(building);
	}

	@GetMapping
	public List<Building> listBuildings() {
		return buildingService.listBuildings();
	}

	@GetMapping("/{id}")
	public Building getBuilding(@PathVariable Long id) {
		return buildingService.findBuildingById(id);
	}

	@PutMapping("/{id}")
	public Building updateBuilding(@PathVariable Long id, @Valid @RequestBody Building building) {
		return buildingService.updateBuilding(id, building);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteBuilding(@PathVariable Long id) {
		buildingService.deleteBuilding(id);
	}
}
