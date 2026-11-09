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
import com.esb.model.Equipment;
import com.esb.service.EquipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipments")
@Tag(name = "5. Equipment")
public class EquipmentController {

	private static final String EXAMPLE = "{\"name\": \"Projector\", \"description\": \"HD video projector with HDMI\"}";
	private static final String VALIDATION = "{\"name\": \"Equipment name is required\"}";
	private static final String DUPLICATE = "{\"error\": \"Equipment name already exists\"}";

	@Autowired
	EquipmentService equipmentService;

	@Operation(summary = "Create an equipment", description = "Equipment is then installed in rooms with `PUT /api/rooms/{roomId}/equipments/{equipmentId}` (Many-to-Many).")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON, examples = {
			@ExampleObject(name = "Projector", value = EXAMPLE),
			@ExampleObject(name = "Computers", value = "{\"name\": \"Computers\", \"description\": \"20 desktop PCs\"}") }))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Equipment created"),
			@ApiResponse(responseCode = "400", description = "Name missing or already used", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Validation", value = VALIDATION),
							@ExampleObject(name = "Duplicate name", value = DUPLICATE) })) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Equipment addEquipment(@Valid @RequestBody Equipment equipment) {
		return equipmentService.addEquipment(equipment);
	}

	@Operation(summary = "List all equipment")
	@GetMapping
	public List<Equipment> listEquipments() {
		return equipmentService.listEquipments();
	}

	@Operation(summary = "Get one equipment")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Equipment found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.EQUIPMENT_NOT_FOUND))) })
	@GetMapping("/{id}")
	public Equipment getEquipment(@Parameter(description = "Equipment id", example = "1") @PathVariable Long id) {
		return equipmentService.findEquipmentById(id);
	}

	@Operation(summary = "Update an equipment")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Update", value = EXAMPLE)))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Equipment updated"),
			@ApiResponse(responseCode = "400", description = "Name missing or already used", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Validation", value = VALIDATION),
							@ExampleObject(name = "Duplicate name", value = DUPLICATE) })),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.EQUIPMENT_NOT_FOUND))) })
	@PutMapping("/{id}")
	public Equipment updateEquipment(@Parameter(description = "Equipment id", example = "1") @PathVariable Long id,
			@Valid @RequestBody Equipment equipment) {
		return equipmentService.updateEquipment(id, equipment);
	}

	@Operation(summary = "Delete an equipment", description = "Refused (409) while it is installed in a room.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Equipment deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.EQUIPMENT_NOT_FOUND))),
			@ApiResponse(responseCode = "409", description = "Equipment installed in a room",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.CONFLICT))) })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteEquipment(@Parameter(description = "Equipment id", example = "1") @PathVariable Long id) {
		equipmentService.deleteEquipment(id);
	}
}
