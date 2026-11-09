package com.esb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.esb.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

	// Custom query 5 (JPQL): teachers of a department, searched by the department name (case-insensitive)
	@Query("SELECT t FROM Teacher t WHERE LOWER(t.department.name) = LOWER(:departmentName)")
	List<Teacher> findByDepartmentName(@Param("departmentName") String departmentName);

	// Teacher email must be unique
	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
