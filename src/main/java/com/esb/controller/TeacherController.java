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

import com.esb.model.Teacher;
import com.esb.service.TeacherService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
	@Autowired
	TeacherService teacherService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Teacher addTeacher(@Valid @RequestBody Teacher teacher) {
		return teacherService.addTeacher(teacher);
	}

	@GetMapping
	public List<Teacher> listTeachers() {
		return teacherService.listTeachers();
	}

	@GetMapping("/{id}")
	public Teacher getTeacher(@PathVariable Long id) {
		return teacherService.findTeacherById(id);
	}

	@PutMapping("/{id}")
	public Teacher updateTeacher(@PathVariable Long id, @Valid @RequestBody Teacher teacher) {
		return teacherService.updateTeacher(id, teacher);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTeacher(@PathVariable Long id) {
		teacherService.deleteTeacher(id);
	}
}
