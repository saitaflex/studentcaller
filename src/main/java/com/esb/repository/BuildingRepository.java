package com.esb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Building;

public interface BuildingRepository extends JpaRepository<Building, Long> {

}
