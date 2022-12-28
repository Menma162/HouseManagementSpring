package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Rate;
import com.HouseManagement.HouseManagement.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Integer> {
    Optional<Rate> findById(Integer id);
}
