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
import com.esb.model.Department;
import com.esb.service.DepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "1. Departments")
public class DepartmentController {

	private static final String EXAMPLE = "{\"name\": \"Computer Science\"}";
	private static final String VALIDATION = "{\"name\": \"Department name is required\"}";

	@Autowired
	DepartmentService departmentService;

	@Operation(summary = "Create a department", description = "Departments group teachers. Create one before adding teachers.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Computer Science", value = EXAMPLE)))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Department created"),
			@ApiResponse(responseCode = "400", description = "Name is missing",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = VALIDATION))) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Department addDepartment(@Valid @RequestBody Department department) {
		return departmentService.addDepartment(department);
	}

	@Operation(summary = "List all departments")
	@GetMapping
	public List<Department> listDepartments() {
		return departmentService.listDepartments();
	}

	@Operation(summary = "Get one department")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Department found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.DEPARTMENT_NOT_FOUND))) })
	@GetMapping("/{id}")
	public Department getDepartment(@Parameter(description = "Department id", example = "1") @PathVariable Long id) {
		return departmentService.findDepartmentById(id);
	}

	@Operation(summary = "Rename a department")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Rename", value = "{\"name\": \"Mathematics\"}")))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Department updated"),
			@ApiResponse(responseCode = "400", description = "Name is missing",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = VALIDATION))),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.DEPARTMENT_NOT_FOUND))) })
	@PutMapping("/{id}")
	public Department updateDepartment(@Parameter(description = "Department id", example = "1") @PathVariable Long id,
			@Valid @RequestBody Department department) {
		return departmentService.updateDepartment(id, department);
	}

	@Operation(summary = "Delete a department", description = "Refused (409) while teachers still belong to it.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Department deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.DEPARTMENT_NOT_FOUND))),
			@ApiResponse(responseCode = "409", description = "Teachers still belong to this department",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.CONFLICT))) })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDepartment(@Parameter(description = "Department id", example = "1") @PathVariable Long id) {
		departmentService.deleteDepartment(id);
	}
}
