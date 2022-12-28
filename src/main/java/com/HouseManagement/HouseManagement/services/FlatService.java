package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.repositories.FlatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FlatService {
    private final FlatRepository flatRepository;
    public FlatService(FlatRepository flatRepository) {
        this.flatRepository = flatRepository;
    }
    public List<Flat> list(){
        return flatRepository.findAll();
    }
    public Optional<Flat> getFlatById(Integer id) {
        return flatRepository.findById(id);
    }
    public List<Flat> getFlatsByPhoneNumber(String phoneNumber){
        return flatRepository.findAllByFlatNumber(phoneNumber);
    }
    public List<Flat> getFlatsByPersonalAccount(String personalAccount){
        return flatRepository.findAllByPersonalAccount(personalAccount);
    }
    public List<Flat> getFlatsByNumberNotId(Integer id, String flatNumber){
        return flatRepository.findAllByFlatNumberAndIdNot(id, flatNumber);
    }
    public List<Flat> getFlatsByPersonalAccountNotId(Integer id, String personalAccount){
        return flatRepository.findAllByPersonalAccountAndIdNot(id, personalAccount);
    }
    public void postFlat(Flat flat){ flatRepository.save(flat); }

    public void deleteFlat(Flat flat){flatRepository.delete(flat);}
    public List<Flat> listByTenant(Tenant tenant){ return flatRepository.findAllByTenant(tenant);}
}
