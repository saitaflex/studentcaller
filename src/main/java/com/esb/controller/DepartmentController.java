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

import com.esb.model.Department;
import com.esb.service.DepartmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
	@Autowired
	DepartmentService departmentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Department addDepartment(@Valid @RequestBody Department department) {
		return departmentService.addDepartment(department);
	}

	@GetMapping
	public List<Department> listDepartments() {
		return departmentService.listDepartments();
	}

	@GetMapping("/{id}")
	public Department getDepartment(@PathVariable Long id) {
		return departmentService.findDepartmentById(id);
	}

	@PutMapping("/{id}")
	public Department updateDepartment(@PathVariable Long id, @Valid @RequestBody Department department) {
		return departmentService.updateDepartment(id, department);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDepartment(@PathVariable Long id) {
		departmentService.deleteDepartment(id);
	}
}
