package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Indication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IndicationRepository extends JpaRepository<Indication, Integer> {
    List<Indication> findAllByCounter(Counter counter);
    List<Indication> findAllByCounterAndPeriod(Counter counter, String period);
    Optional<Indication> findByCounterAndPeriod(Counter counter, String period);
}
