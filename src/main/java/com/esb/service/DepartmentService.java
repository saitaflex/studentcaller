package com.esb.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.model.Department;
import com.esb.repository.DepartmentRepository;

@Service
public class DepartmentService {
	@Autowired
	DepartmentRepository departmentRepo;

	public Department addDepartment(Department department) {
		return departmentRepo.save(department);
	}

	public List<Department> listDepartments() {
		return departmentRepo.findAll();
	}

	public Department findDepartmentById(Long id) {
		return departmentRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Department not found"));
	}

	public Department updateDepartment(Long id, Department newData) {
		Department department = findDepartmentById(id);
		department.setName(newData.getName());
		return departmentRepo.save(department);
	}

	public void deleteDepartment(Long id) {
		departmentRepo.delete(findDepartmentById(id));
	}
}
