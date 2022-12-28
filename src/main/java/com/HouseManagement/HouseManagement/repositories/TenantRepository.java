package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TenantRepository  extends JpaRepository<Tenant, Integer> {
    Optional<Tenant> findById(Integer id);
    List<Tenant> findAllByEmail(String email);
    @Query("select b from Tenant b where b.email = :email and b.id != :id")
    List<Tenant> findAllByEmailAndIdNot(Integer id, String email);
    @Query("select b from Tenant b where b.phoneNumber = :phoneNumber and b.id != :id")
    List<Tenant> findAllByPhoneNumberAndIdNot(Integer id, String phoneNumber);
    List<Tenant> findAllByPhoneNumber(String phone_number);
}
