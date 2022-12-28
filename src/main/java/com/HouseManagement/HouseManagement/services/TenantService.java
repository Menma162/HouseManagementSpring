package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.repositories.FlatRepository;
import com.HouseManagement.HouseManagement.repositories.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final FlatRepository flatRepository;

    public TenantService(TenantRepository tenantRepository, FlatRepository flatRepository) {
        this.tenantRepository = tenantRepository;
        this.flatRepository = flatRepository;
    }
    public List<Tenant> list(){
        return tenantRepository.findAll();
    }
    public void postTenant(Tenant tenant){ tenantRepository.save(tenant); }
    public Optional<Tenant> getTenantById(Integer id) {
        return tenantRepository.findById(id);
    }
    public List<Tenant> getTenantsByEmail(String email) {
        return tenantRepository.findAllByEmail(email);
    }
    public List<Tenant> findAllByPhoneNumber(String phone_number) {return tenantRepository.findAllByPhoneNumber(phone_number);}
    public List<Tenant> getTenantsByEmailNotId(Integer id, String email) {return tenantRepository.findAllByEmailAndIdNot(id, email);}
    public List<Tenant> getTenantsByPhoneNumberNotId(Integer id, String phoneNumber) {return tenantRepository.findAllByPhoneNumberAndIdNot(id, phoneNumber);}
    public void deleteTenant(Tenant tenant){tenantRepository.delete(tenant);}
    public List<Flat> findAllByTenant(Integer id) {return flatRepository.findAllByTenant(tenantRepository.findById(id).get());}

}
