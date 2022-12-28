package com.HouseManagement.HouseManagement.repositories;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Payment;
import com.HouseManagement.HouseManagement.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findAllByFlat(Flat flat);
    List<Payment> findAllByFlatAndPeriod(Flat flat, String period);
    List<Payment> findAllByPeriod(String period);
}
