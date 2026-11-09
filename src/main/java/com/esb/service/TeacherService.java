package com.esb.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.model.Teacher;
import com.esb.repository.TeacherRepository;

@Service
public class TeacherService {

	@Autowired
	TeacherRepository teacherRepo;

	@Autowired
	DepartmentService departmentService;

	public Teacher addTeacher(Teacher teacher) {
		if (teacherRepo.existsByEmailIgnoreCase(teacher.getEmail())) {
			throw new IllegalStateException("Email is already used by another teacher");
		}
		teacher.setDepartment(departmentService.findDepartmentById(departmentId(teacher)));
		return teacherRepo.save(teacher);
	}

	public List<Teacher> listTeachers() {
		return teacherRepo.findAll();
	}

	public List<Teacher> listTeachersByDepartmentName(String departmentName) {
		return teacherRepo.findByDepartmentName(departmentName);
	}

	public Teacher findTeacherById(Long id) {
		return teacherRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Teacher not found"));
	}

	public Teacher updateTeacher(Long id, Teacher newData) {
		Teacher teacher = findTeacherById(id);
		if (teacherRepo.existsByEmailIgnoreCaseAndIdNot(newData.getEmail(), id)) {
			throw new IllegalStateException("Email is already used by another teacher");
		}
		teacher.setName(newData.getName());
		teacher.setEmail(newData.getEmail());
		teacher.setDepartment(departmentService.findDepartmentById(departmentId(newData)));
		return teacherRepo.save(teacher);
	}

	public void deleteTeacher(Long id) {
		teacherRepo.delete(findTeacherById(id));
	}

	private Long departmentId(Teacher teacher) {
		if (teacher.getDepartment() == null || teacher.getDepartment().getId() == null) {
			throw new IllegalStateException("Department id is required");
		}
		return teacher.getDepartment().getId();
	}
}
