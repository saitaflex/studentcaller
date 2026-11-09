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

import com.esb.config.ApiDocs;
import com.esb.model.Building;
import com.esb.service.BuildingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/buildings")
@Tag(name = "3. Buildings")
public class BuildingController {

	private static final String EXAMPLE = "{\"name\": \"Bloc A\", \"address\": \"Campus Nord, Tunis\"}";
	private static final String VALIDATION = "{\"name\": \"Building name is required\"}";

	@Autowired
	BuildingService buildingService;

	@Operation(summary = "Create a building", description = "Buildings contain rooms. Create one before adding rooms.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Bloc A", value = EXAMPLE)))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Building created"),
			@ApiResponse(responseCode = "400", description = "Name is missing",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = VALIDATION))) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Building addBuilding(@Valid @RequestBody Building building) {
		return buildingService.addBuilding(building);
	}

	@Operation(summary = "List all buildings")
	@GetMapping
	public List<Building> listBuildings() {
		return buildingService.listBuildings();
	}

	@Operation(summary = "Get one building")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Building found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.BUILDING_NOT_FOUND))) })
	@GetMapping("/{id}")
	public Building getBuilding(@Parameter(description = "Building id", example = "1") @PathVariable Long id) {
		return buildingService.findBuildingById(id);
	}

	@Operation(summary = "Update a building")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Update", value = EXAMPLE)))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Building updated"),
			@ApiResponse(responseCode = "400", description = "Name is missing",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = VALIDATION))),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.BUILDING_NOT_FOUND))) })
	@PutMapping("/{id}")
	public Building updateBuilding(@Parameter(description = "Building id", example = "1") @PathVariable Long id,
			@Valid @RequestBody Building building) {
		return buildingService.updateBuilding(id, building);
	}

	@Operation(summary = "Delete a building", description = "Refused (409) while rooms are still in it.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Building deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.BUILDING_NOT_FOUND))),
			@ApiResponse(responseCode = "409", description = "Building still has rooms",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.CONFLICT))) })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteBuilding(@Parameter(description = "Building id", example = "1") @PathVariable Long id) {
		buildingService.deleteBuilding(id);
	}
}
