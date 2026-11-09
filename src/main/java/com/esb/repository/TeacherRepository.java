package com.esb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

	// Custom queries 6-7: teacher email must be unique
	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
