package com.HouseManagement.HouseManagement.services;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Payment;
import com.HouseManagement.HouseManagement.repositories.FlatRepository;
import com.HouseManagement.HouseManagement.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final FlatRepository flatRepository;

    public PaymentService(PaymentRepository paymentRepository, FlatRepository flatRepository) {
        this.paymentRepository = paymentRepository;
        this.flatRepository = flatRepository;
    }

    public List<Payment> findAllByFlat(Integer id) {return paymentRepository.findAllByFlat(flatRepository.findById(id).get());}
    public List<Payment> findAllByFlatAndPeriod(Flat flat, String period){return paymentRepository.findAllByFlatAndPeriod(flat, period);}
    public List<Payment> findAllByPeriod(String period){return paymentRepository.findAllByPeriod(period);}
    public List<Payment> list(){
        return paymentRepository.findAll();
    }
    public Optional<Payment> getPaymentById(Integer id) {
        return paymentRepository.findById(id);
    }
    public void postPayment(Payment payment){ paymentRepository.save(payment); }

}
