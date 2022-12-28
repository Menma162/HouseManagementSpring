package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.repositories.CounterRepository;
import com.HouseManagement.HouseManagement.repositories.FlatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CounterService {
    private final CounterRepository counterRepository;
    private final FlatRepository flatRepository;

    public CounterService(CounterRepository counterRepository, FlatRepository flatRepository) {
        this.counterRepository = counterRepository;
        this.flatRepository = flatRepository;
    }
    public Optional<Counter> getCounterById(Integer id) {
        return counterRepository.findById(id);
    }

    public List<Counter> findAllByFlat(Integer id) {return counterRepository.findAllByFlat(flatRepository.findById(id).get());}
    public List<Counter> findAllByFlatAndUsed(Integer id) {return counterRepository.findAllByFlatAndUsed(flatRepository.findById(id).get(), true);}

    public List<Counter> list(){
        return counterRepository.findAll();
    }

    public List<Counter> findAllByTypeAndNumber(String type, String number) {return counterRepository.findAllByTypeAndNumber(type, number);}
    public void postCounter(Counter counter){ counterRepository.save(counter); }
    public List<Counter> findAllByTypeAndNumberIdNot(Integer id, String type, String number) {return  counterRepository.findAllByTypeAndNumberIdNot(id, type, number);}

    public void deleteCounter(Integer id){counterRepository.deleteById(id);}
}
