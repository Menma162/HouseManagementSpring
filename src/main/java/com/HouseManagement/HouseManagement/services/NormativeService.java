package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Normative;
import com.HouseManagement.HouseManagement.models.Rate;
import com.HouseManagement.HouseManagement.repositories.NormativeRepository;
import com.HouseManagement.HouseManagement.repositories.RateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NormativeService {
    private final NormativeRepository normativeRepository;

    public NormativeService(NormativeRepository normativeRepository) {
        this.normativeRepository = normativeRepository;
    }
    public List<Normative> list(){
        return normativeRepository.findAll();
    }
    public void postNormative(Normative normative){ normativeRepository.save(normative); }
    public Optional<Normative> getNormativeById(Integer id) {
        return normativeRepository.findById(id);
    }

}
