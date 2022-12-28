package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Normative;
import com.HouseManagement.HouseManagement.models.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NormativeRepository extends JpaRepository<Normative, Integer> {
    Optional<Normative> findById(Integer id);
}
