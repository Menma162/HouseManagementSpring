package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Rate;
import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.repositories.FlatRepository;
import com.HouseManagement.HouseManagement.repositories.RateRepository;
import com.HouseManagement.HouseManagement.repositories.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RateService {
    private final RateRepository rateRepository;

    public RateService(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }
    public List<Rate> list(){
        return rateRepository.findAll();
    }
    public void postRate(Rate rate){ rateRepository.save(rate); }
    public Optional<Rate> getRateById(Integer id) {
        return rateRepository.findById(id);
    }

}
