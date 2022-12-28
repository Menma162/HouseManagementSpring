package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.*;
import com.HouseManagement.HouseManagement.other.ConvertImage;
import com.HouseManagement.HouseManagement.other.FilterSearchData;
import com.HouseManagement.HouseManagement.other.Period;
import com.HouseManagement.HouseManagement.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/Payment")
public class PaymentController {

    private final FlatService flatService;
    private final PaymentService paymentService;
    private final RateService rateService;
    private final NormativeService normativeService;
    private final UserService userService;
    private final TenantService tenantService;
    private final CounterService counterService;
    private final IndicationService indicationService;
    private static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "\\uploads";
    private final String[] types = new String[]{"Холодная вода", "Горячая вода", "Электроэнергия", "Газ", "Тепловая энергия"};
    private final String[] statuses = new String[]{"Оплачено", "Не оплачено"};


    public PaymentController(FlatService flatService, PaymentService paymentService, RateService rateService, NormativeService normativeService, UserService userService, TenantService tenantService, CounterService counterService, IndicationService indicationService) {
        this.paymentService = paymentService;
        this.flatService = flatService;
        this.rateService = rateService;
        this.normativeService = normativeService;
        this.userService = userService;
        this.tenantService = tenantService;
        this.counterService = counterService;
        this.indicationService = indicationService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Payment> payments = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) payments = paymentService.list();
        else
        {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                payments.addAll(paymentService.findAllByFlat(flat.getId()));
            }
        }
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("payments", payments);
        model.addAttribute("filterSearchData", new FilterSearchData());
        model.addAttribute("types", types);
        model.addAttribute("statuses", statuses);
        return "payment/payments";
    }

    @GetMapping("/details/{id}")
    public String detailsPage(Model model, @PathVariable("id") Integer id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Payment payment = paymentService.getPaymentById(id).get();
        List<Payment> payments = new ArrayList<Payment>();
        if(user.getAuthority().equals("USER")) {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                payments.addAll(paymentService.findAllByFlat(flat.getId()));
            }
        }
        String path = UPLOAD_DIRECTORY + "\\" + payment.getCheque();
        if(payment.getCheque() != null && (new File(path).isFile())) model.addAttribute("photo", ConvertImage.toBase64(path));
        else model.addAttribute("photo", null);
        model.addAttribute("payment", payment);
        if (payments.contains(payment) || user.getAuthority().equals("ADMIN")) return "payment/details";
        else return "redirect:/Payment/list";
    }

    @PostMapping("/search")
    public String search(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Payment> payments = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) payments = paymentService.list();
        else
        {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                payments.addAll(paymentService.findAllByFlat(flat.getId()));
            }
        }
        if(!Objects.equals(filterSearchData.getSearch(), "")) payments = payments.stream().filter(x -> x.getFlat().getFlatNumber().contains(filterSearchData.getSearch())).toList();
        if(!Objects.equals(filterSearchData.getType(), "${type}")) payments = payments.stream().filter(x -> x.getRate().getName().equals(filterSearchData.getType())).toList();
        boolean status = filterSearchData.getStatus().equals("Оплачено");
        if (!Objects.equals(filterSearchData.getStatus(), "${status}")) payments = payments.stream().filter(x -> x.isStatus() == status).toList();
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("payments",payments);
        model.addAttribute("types", types);
        model.addAttribute("statuses", statuses);
        return "payment/payments";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("period", Period.getPeriod());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "payment/create";
        else return "redirect:/Payment/list";
    }

    @PostMapping("/create")
    public String createPayment(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            String period = Period.getPeriod();
            boolean error = paymentService.findAllByPeriod(period).size() != 0;
            model.addAttribute("period", period);
            model.addAttribute("error", error);
            model.addAttribute("errorName", "Начисления невозможны, так как за этот период они уже произведены");
            if (error) return "payment/create";
            addPayments(period);
        }
        return "redirect:/Payment/list";
    }

    @GetMapping("/update/{id}")
    public String updatePage(Model model, @PathVariable("id") Integer id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Payment payment = paymentService.getPaymentById(id).get();
        List<Payment> payments = new ArrayList<Payment>();
        if(user.getAuthority().equals("USER")) {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                payments.addAll(paymentService.findAllByFlat(flat.getId()));
            }
        }
        String path = UPLOAD_DIRECTORY + "\\" + payment.getCheque();
        if(payment.getCheque() != null && (new File(path).isFile())) model.addAttribute("photo", ConvertImage.toBase64(path));
        else model.addAttribute("photo", null);
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("flat", payment.getFlat());
        model.addAttribute("rate", payment.getRate());
        model.addAttribute("payment", payment);
        if (payments.contains(payment) || user.getAuthority().equals("ADMIN")) return "payment/update";
        else return "redirect:/Payment/list";
    }

    @PostMapping("/update/{id}")
    public String updatePayment(@ModelAttribute("payment") @Valid Payment payment, BindingResult bindingResult, Model model, @PathVariable("id") Integer id, @RequestParam("image") MultipartFile file) throws IOException {
        String path = UPLOAD_DIRECTORY + "\\" + payment.getCheque();
        if(payment.getCheque() != null && (new File(path).isFile())) model.addAttribute("photo", ConvertImage.toBase64(path));
        model.addAttribute("flat", payment.getFlat());
        model.addAttribute("rate", payment.getRate());
        model.addAttribute("payment", payment);
        String role = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        if(bindingResult.hasErrors()) return "payment/update";
        StringBuilder fileNames = new StringBuilder();
        String name =   payment.getId() + "_" + file.getOriginalFilename();
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, name);
        fileNames.append(file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        if (!file.isEmpty()) payment.setCheque(name);
        paymentService.postPayment(payment);

        return "redirect:/Payment/details/" + id;
    }

    private void addPayments(String period) {
        List<Flat> flats = flatService.list();
        List<Rate> rates = rateService.list();
        List<Normative> normatives = normativeService.list();

        if(flats.size() != 0){
            for (Flat flat: flats) {
                List<Counter> counters = counterService.findAllByFlatAndUsed(flat.getId());

                List<Indication> indications = new ArrayList<Indication>();

                for(Counter counter: counters) {
                    var item = indicationService.findByCounterAndPeriod(counter, period);
                    item.ifPresent(indications::add);
                }

                Payment payment = new Payment();
                Payment payment_cw = new Payment();
                Payment payment_hw = new Payment();
                Payment payment_ee = new Payment();
                Payment payment_g = new Payment();
                Payment payment_te = new Payment();

                int count_cw = 0, count_hw = 0, count_ee = 0, count_g = 0, count_te = 0;
                float sum_ind_cw = 0;
                float sum_noind_cw = 0;
                float sum_ind_hw = 0;
                float sum_noind_hw = 0;
                float sum_ind_ee = 0;
                float sum_noind_ee = 0;
                float sum_ind_g = 0;
                float sum_noind_g = 0;
                float sum_ind_te = 0;
                float sum_noind_te = 0;

                for(Counter counter: counters){
                    String type = counter.getType();
                    switch (type) {
                        case "Счетчик холодной воды" -> {
                            Rate rate = rates.stream().filter(x -> x.getName().equals("Холодная вода")).findFirst().orElse(null);
                            Normative normative = normatives.stream().filter(x -> x.getName().equals("Холодная вода")).findFirst().orElse(null);
                            Indication indication = indications.stream().filter(x -> x.getCounter() == counter).findFirst().orElse(null);
                            payment_cw.setPeriod(period);
                            payment_cw.setStatus(false);
                            payment_cw.setCheque(null);
                            payment_cw.setFlat(flat);
                            payment_cw.setRate(rate);
                            payment_cw.setNormative(normative);
                            if (indication != null) {
                                sum_ind_cw += indication.getValue() * rate.getValue();
                            } else {
                                if (flat.getNumberOfRegisteredResidents() != 0)
                                    sum_noind_cw = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                                else sum_noind_cw = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                                count_cw++;
                            }
                        }
                        case "Счетчик горячей воды" -> {
                            Rate rate = rates.stream().filter(x -> x.getName().equals("Горячая вода")).findFirst().orElse(null);
                            Normative normative = normatives.stream().filter(x -> x.getName().equals("Горячая вода")).findFirst().orElse(null);
                            Indication indication = indications.stream().filter(x -> x.getCounter() == counter).findFirst().orElse(null);
                            payment_hw.setPeriod(period);
                            payment_hw.setStatus(false);
                            payment_hw.setCheque(null);
                            payment_hw.setFlat(flat);
                            payment_hw.setRate(rate);
                            payment_hw.setNormative(normative);
                            if (indication != null) {
                                sum_ind_hw += indication.getValue() * rate.getValue();
                            } else {
                                if (flat.getNumberOfRegisteredResidents() != 0)
                                    sum_noind_hw = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                                else sum_noind_hw = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                                count_hw++;
                            }
                        }
                        case "Счетчик электрической энергии" -> {
                            Rate rate = rates.stream().filter(x -> x.getName().equals("Электроэнергия")).findFirst().orElse(null);
                            Normative normative = normatives.stream().filter(x -> x.getName().equals("Электроэнергия")).findFirst().orElse(null);
                            Indication indication = indications.stream().filter(x -> x.getCounter() == counter).findFirst().orElse(null);
                            payment_ee.setPeriod(period);
                            payment_ee.setStatus(false);
                            payment_ee.setCheque(null);
                            payment_ee.setFlat(flat);
                            payment_ee.setRate(rate);
                            payment_ee.setNormative(normative);
                            if (indication != null) {
                                sum_ind_ee += indication.getValue() * rate.getValue();
                            } else {
                                if (flat.getNumberOfRegisteredResidents() != 0)
                                    sum_noind_ee = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                                else sum_noind_ee = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                                count_ee++;
                            }
                        }
                        case "Газовый счетчик" -> {
                            Rate rate = rates.stream().filter(x -> x.getName().equals("Газ")).findFirst().orElse(null);
                            Normative normative = normatives.stream().filter(x -> x.getName().equals("Газ")).findFirst().orElse(null);
                            Indication indication = indications.stream().filter(x -> x.getCounter() == counter).findFirst().orElse(null);
                            payment_g.setPeriod(period);
                            payment_g.setStatus(false);
                            payment_g.setCheque(null);
                            payment_g.setFlat(flat);
                            payment_g.setRate(rate);
                            payment_g.setNormative(normative);
                            if (indication != null) {
                                sum_ind_g += indication.getValue() * rate.getValue();
                            } else {
                                if (flat.getNumberOfRegisteredResidents() != 0)
                                    sum_noind_g = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                                else sum_noind_g = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                                count_g++;
                            }
                        }
                        case "Счетчик отопления" -> {
                            Rate rate = rates.stream().filter(x -> x.getName().equals("Тепловая энергия")).findFirst().orElse(null);
                            Normative normative = normatives.stream().filter(x -> x.getName().equals("Тепловая энергия")).findFirst().orElse(null);
                            Indication indication = indications.stream().filter(x -> x.getCounter() == counter).findFirst().orElse(null);
                            payment_te.setPeriod(period);
                            payment_te.setStatus(false);
                            payment_te.setCheque(null);
                            payment_te.setFlat(flat);
                            payment_te.setRate(rate);
                            payment_te.setNormative(normative);
                            if (indication != null) {
                                sum_ind_te += indication.getValue() * rate.getValue();
                            } else {
                                sum_ind_te = normative.getValue() * flat.getUsablea();
                                count_te++;
                            }
                        }
                    }
                }
                int found = 0;
                found = Math.toIntExact(counters.stream().filter(x -> x.getType().equals("Счетчик холодной воды")).count());
                payment = new Payment();
                if(found == 0){
                    Rate rate = rates.stream().filter(x -> x.getName().equals("Холодная вода")).findFirst().orElse(null);
                    Normative normative = normatives.stream().filter(x -> x.getName().equals("Холодная вода")).findFirst().orElse(null);
                    Float amount = null;

                    if(flat.getNumberOfRegisteredResidents() != 0) amount = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                    else amount = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                    amount = new BigDecimal(amount).setScale(1, RoundingMode.HALF_EVEN ).floatValue();

                    payment.setPeriod(period);
                    payment.setStatus(false);
                    payment.setCheque(null);
                    payment.setFlat(flat);
                    payment.setRate(rate);
                    payment.setNormative(normative);
                    payment.setAmount(amount);

                    paymentService.postPayment(payment);
                }
                else{
                    if(count_cw != 0)  payment_cw.setAmount(new BigDecimal(sum_ind_cw + (sum_noind_cw / count_cw)).setScale(1, RoundingMode.HALF_EVEN ).floatValue());
                    else payment_cw.setAmount(new BigDecimal(sum_ind_cw).setScale(1, RoundingMode.HALF_EVEN ).floatValue());

                    paymentService.postPayment(payment_cw);
                }
                found = 0;
                payment = new Payment();
                found = Math.toIntExact(counters.stream().filter(x -> x.getType().equals("Счетчик горячей воды")).count());
                if(found == 0){
                    Rate rate = rates.stream().filter(x -> x.getName().equals("Горячая вода")).findFirst().orElse(null);
                    Normative normative = normatives.stream().filter(x -> x.getName().equals("Горячая вода")).findFirst().orElse(null);
                    Float amount = null;

                    if(flat.getNumberOfRegisteredResidents() != 0) amount = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                    else amount = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                    amount = new BigDecimal(amount).setScale(1, RoundingMode.HALF_EVEN ).floatValue();

                    payment.setPeriod(period);
                    payment.setStatus(false);
                    payment.setCheque(null);
                    payment.setFlat(flat);
                    payment.setRate(rate);
                    payment.setNormative(normative);
                    payment.setAmount(amount);

                    paymentService.postPayment(payment);
                }
                else{
                    if(count_hw != 0)  payment_hw.setAmount(new BigDecimal(sum_ind_hw + (sum_noind_hw / count_hw)).setScale(1, RoundingMode.HALF_EVEN ).floatValue());
                    else payment_hw.setAmount(new BigDecimal(sum_ind_hw).setScale(1, RoundingMode.HALF_EVEN ).floatValue());

                    paymentService.postPayment(payment_hw);
                }
                found = 0;
                payment = new Payment();
                found = Math.toIntExact(counters.stream().filter(x -> x.getType().equals("Счетчик электрической энергии")).count());
                if(found == 0){
                    Rate rate = rates.stream().filter(x -> x.getName().equals("Электроэнергия")).findFirst().orElse(null);
                    Normative normative = normatives.stream().filter(x -> x.getName().equals("Электроэнергия")).findFirst().orElse(null);
                    Float amount = null;

                    if(flat.getNumberOfRegisteredResidents() != 0) amount = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                    else amount = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                    amount = new BigDecimal(amount).setScale(1, RoundingMode.HALF_EVEN ).floatValue();

                    payment.setPeriod(period);
                    payment.setStatus(false);
                    payment.setCheque(null);
                    payment.setFlat(flat);
                    payment.setRate(rate);
                    payment.setNormative(normative);
                    payment.setAmount(amount);

                    paymentService.postPayment(payment);
                }
                else{
                    if(count_ee != 0)  payment_ee.setAmount(new BigDecimal(sum_ind_ee + (sum_noind_ee / count_ee)).setScale(1, RoundingMode.HALF_EVEN ).floatValue());
                    else payment_ee.setAmount(new BigDecimal(sum_ind_ee).setScale(1, RoundingMode.HALF_EVEN ).floatValue());


                    paymentService.postPayment(payment_ee);
                }
                found = 0;
                payment = new Payment();
                found = Math.toIntExact(counters.stream().filter(x -> x.getType().equals("Газовый счетчик")).count());
                if(found == 0){

                    Rate rate = rates.stream().filter(x -> x.getName().equals("Газ")).findFirst().orElse(null);
                    Normative normative = normatives.stream().filter(x -> x.getName().equals("Газ")).findFirst().orElse(null);
                    Float amount = null;

                    if(flat.getNumberOfRegisteredResidents() != 0) amount = rate.getValue() * normative.getValue() * flat.getNumberOfRegisteredResidents();
                    else amount = rate.getValue() * normative.getValue() * flat.getNumberOfOwners();
                    amount = new BigDecimal(amount).setScale(1, RoundingMode.HALF_EVEN ).floatValue();

                    payment.setPeriod(period);
                    payment.setStatus(false);
                    payment.setCheque(null);
                    payment.setFlat(flat);
                    payment.setRate(rate);
                    payment.setNormative(normative);
                    payment.setAmount(amount);

                    paymentService.postPayment(payment);
                }
                else{
                    if(count_g != 0)  payment_g.setAmount(new BigDecimal(sum_ind_g + (sum_noind_g / count_g)).setScale(1, RoundingMode.HALF_EVEN ).floatValue());
                    else payment_g.setAmount(new BigDecimal(sum_ind_g).setScale(1, RoundingMode.HALF_EVEN ).floatValue());


                    paymentService.postPayment(payment_g);
                }
                found = 0;
                payment = new Payment();
                found = Math.toIntExact(counters.stream().filter(x -> x.getType().equals("Счетчик отопления")).count());
                if(found == 0){
                    Rate rate = rates.stream().filter(x -> x.getName().equals("Тепловая энергия")).findFirst().orElse(null);
                    Normative normative = normatives.stream().filter(x -> x.getName().equals("Тепловая энергия")).findFirst().orElse(null);
                    float amount = normative.getValue() * flat.getUsablea();
                    amount = new BigDecimal(amount).setScale(1, RoundingMode.HALF_EVEN ).floatValue();

                    payment.setPeriod(period);
                    payment.setStatus(false);
                    payment.setCheque(null);
                    payment.setFlat(flat);
                    payment.setRate(rate);
                    payment.setNormative(normative);
                    payment.setAmount(amount);

                    paymentService.postPayment(payment);
                }
                else{
                    if(count_te != 0)  payment_te.setAmount(new BigDecimal(sum_ind_te + (sum_noind_te / count_te)).setScale(1, RoundingMode.HALF_EVEN ).floatValue());
                    else payment_te.setAmount(new BigDecimal(sum_ind_te).setScale(1, RoundingMode.HALF_EVEN ).floatValue());


                    paymentService.postPayment(payment_te);
                }
            }
        }
    }
}
