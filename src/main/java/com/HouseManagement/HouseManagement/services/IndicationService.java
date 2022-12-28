package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Indication;
import com.HouseManagement.HouseManagement.repositories.IndicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IndicationService {
    private final IndicationRepository indicationRepository;

    public IndicationService(IndicationRepository indicationRepository) {
        this.indicationRepository = indicationRepository;
    }

    public List<Indication> list() {
        return indicationRepository.findAll();
    }

    public List<Indication> findAllByCounter(Counter counter) {
        return indicationRepository.findAllByCounter(counter);
    }

    public Optional<Indication> getIndicationById(Integer id) {
        return indicationRepository.findById(id);
    }

    public List<Indication> findAllByCounterAndPeriod(Counter counter, String period) {
        return indicationRepository.findAllByCounterAndPeriod(counter, period);
    }

    public Optional<Indication> findByCounterAndPeriod(Counter counter, String period) {return indicationRepository.findByCounterAndPeriod(counter, period);}

    public void postIndication(Indication indication) {
        indicationRepository.save(indication);
    }

    public void deleteIndication(Integer id) {
        indicationRepository.deleteById(id);
    }
}
