package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, Integer> {
    List<Counter> findAllByFlat(Flat flat);
    List<Counter> findAllByFlatAndUsed(Flat flat, boolean used);
    List<Counter> findAllByTypeAndNumber(String type, String number);
    @Query("select b from Counter b where (b.type = :type and b.number = :number) and b.id != :id")
    List<Counter> findAllByTypeAndNumberIdNot(Integer id, String number, String type);
}
