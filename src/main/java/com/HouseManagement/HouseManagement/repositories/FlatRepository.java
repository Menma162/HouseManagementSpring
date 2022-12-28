package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FlatRepository extends JpaRepository<Flat, Integer> {
    List<Flat> findAllByFlatNumber(String flatNumber);
    List<Flat> findAllByPersonalAccount(String personalAccount);
    List<Flat> findAllByTenant(Tenant tenant);
    @Query("select b from Flat b where b.flatNumber = :flatNumber and b.id != :id")
    List<Flat> findAllByFlatNumberAndIdNot(Integer id, String flatNumber);
    @Query("select b from Flat b where b.personalAccount = :personalAccount and b.id != :id")
    List<Flat> findAllByPersonalAccountAndIdNot(Integer id, String personalAccount);
}
