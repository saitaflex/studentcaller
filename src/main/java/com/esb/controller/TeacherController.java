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
import com.esb.model.Teacher;
import com.esb.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "2. Teachers")
public class TeacherController {

	private static final String EXAMPLE = "{\"name\": \"Sami Ben Ali\", \"email\": \"sami.benali@esb.tn\", \"department\": {\"id\": 1}}";
	private static final String VALIDATION = "{\"email\": \"Email is not valid\", \"name\": \"Name is required\"}";
	private static final String DUPLICATE = "{\"error\": \"Email is already used by another teacher\"}";

	@Autowired
	TeacherService teacherService;

	@Operation(summary = "Create a teacher", description = "Only the department `id` is needed inside `department`. The email must be unique.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "New teacher", value = EXAMPLE)))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Teacher created"),
			@ApiResponse(responseCode = "400", description = "Invalid field or email already used", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Validation", value = VALIDATION),
							@ExampleObject(name = "Duplicate email", value = DUPLICATE) })),
			@ApiResponse(responseCode = "404", description = "Department does not exist",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.DEPARTMENT_NOT_FOUND))) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Teacher addTeacher(@Valid @RequestBody Teacher teacher) {
		return teacherService.addTeacher(teacher);
	}

	@Operation(summary = "List all teachers", description = "Each teacher includes its department.")
	@GetMapping
	public List<Teacher> listTeachers() {
		return teacherService.listTeachers();
	}

	@Operation(summary = "Get one teacher")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Teacher found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.TEACHER_NOT_FOUND))) })
	@GetMapping("/{id}")
	public Teacher getTeacher(@Parameter(description = "Teacher id", example = "1") @PathVariable Long id) {
		return teacherService.findTeacherById(id);
	}

	@Operation(summary = "Update a teacher", description = "Replaces name, email and department.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Update", value = EXAMPLE)))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Teacher updated"),
			@ApiResponse(responseCode = "400", description = "Invalid field or email already used", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Validation", value = VALIDATION),
							@ExampleObject(name = "Duplicate email", value = DUPLICATE) })),
			@ApiResponse(responseCode = "404", description = "Teacher or department not found",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.TEACHER_NOT_FOUND))) })
	@PutMapping("/{id}")
	public Teacher updateTeacher(@Parameter(description = "Teacher id", example = "1") @PathVariable Long id,
			@Valid @RequestBody Teacher teacher) {
		return teacherService.updateTeacher(id, teacher);
	}

	@Operation(summary = "Delete a teacher", description = "Refused (409) while the teacher has reservations.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Teacher deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.TEACHER_NOT_FOUND))),
			@ApiResponse(responseCode = "409", description = "Teacher has reservations",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.CONFLICT))) })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTeacher(@Parameter(description = "Teacher id", example = "1") @PathVariable Long id) {
		teacherService.deleteTeacher(id);
	}
}
