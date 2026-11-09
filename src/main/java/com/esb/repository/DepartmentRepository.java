package com.esb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
