package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Indication;
import com.HouseManagement.HouseManagement.models.User;
import com.HouseManagement.HouseManagement.other.FilterSearchData;
import com.HouseManagement.HouseManagement.other.Period;
import com.HouseManagement.HouseManagement.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/Indication")
public class IndicationController {
    private final CounterService counterService;
    private final IndicationService indicationService;
    private final FlatService flatService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final TenantService tenantService;
    private final String[] types = new String[]{"Счетчик холодной воды", "Счетчик горячей воды", "Счетчик электрической энергии", "Газовый счетчик", "Счетчик отопления"};

    public IndicationController(CounterService counterService, IndicationService indicationService, FlatService flatService, PaymentService paymentService, UserService userService, TenantService tenantService) {
        this.counterService = counterService;
        this.indicationService = indicationService;
        this.flatService = flatService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.tenantService = tenantService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Indication> indications = new ArrayList<>();
        List<Counter> counters = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) indications = indicationService.list();
        else
        {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                counters.addAll(counterService.findAllByFlat(flat.getId()));
            }
            for(Counter counter:counters){
                indications.addAll(indicationService.findAllByCounter(counter));
            }
        }

        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("indications", indications);
        model.addAttribute("filterSearchData", new FilterSearchData());
        model.addAttribute("types", types);
        return "indication/indications";
    }

    @PostMapping("/search")
    public String search(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Indication> indications = new ArrayList<>();
        List<Counter> counters = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) indications = indicationService.list();
        else
        {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                counters.addAll(counterService.findAllByFlat(flat.getId()));
            }
            for(Counter counter:counters){
                indications.addAll(indicationService.findAllByCounter(counter));
            }
        }
        if(!Objects.equals(filterSearchData.getSearch(), "")) indications = indications.stream().filter(x -> x.getCounter().getFlat().getFlatNumber().contains(filterSearchData.getSearch())).toList();
        if(!Objects.equals(filterSearchData.getSearchPeriod(), "")) indications = indications.stream().filter(x -> x.getPeriod().contains(filterSearchData.getSearchPeriod())).toList();
        if(!Objects.equals(filterSearchData.getType(), "${type}")) indications = indications.stream().filter(x -> x.getCounter().getType().equals(filterSearchData.getType())).toList();

        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("indications",indications);
        model.addAttribute("types", types);
        return "indication/indications";
    }

    @GetMapping("/create")
    public String selectFlatPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Flat> flats = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) flats = flatService.list();
        else flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        model.addAttribute("flats", flats);
        model.addAttribute("filterSearchData", new FilterSearchData());
        return "indication/selectFlat";
    }

    @PostMapping("/create")
    public String selectFlat(@ModelAttribute("flat") Flat flat){
        return "redirect:/Indication/" + flat.getId() + "/selectCounter";
    }

    @PostMapping("/create/search")
    public String searchFlat(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Flat> flats = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) flats = flatService.list();
        else flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        if(filterSearchData.getSearch() != null) flats = flats.stream().filter(x -> x.getFlatNumber().contains(filterSearchData.getSearch())).toList();
        model.addAttribute("flats",flats);
        return "indication/selectFlat";
    }

    @GetMapping("/{id}/selectCounter")
    public String selectCounterPage(@PathVariable("id") Integer id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Flat flat = flatService.getFlatById(id).get();
        List<Flat> flats = new ArrayList<Flat>();
        if(user.getAuthority().equals("USER")) flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        model.addAttribute("flat", flat);
        model.addAttribute("counters", counterService.findAllByFlatAndUsed(id));
        if (flats.contains(flat) || user.getAuthority().equals("ADMIN")) return "indication/selectCounter";
        else return "redirect:/Indication/create";
    }

    @PostMapping("/{idFlat}/selectCounter")
    public String selectCounter(@ModelAttribute("flat") Flat flat, @ModelAttribute("counter") Counter counter){
        return "redirect:/Indication/" + flat.getId() + "/" + counter.getId();
    }

    @GetMapping("/{idFlat}/{idCounter}")
    public String createPageAddInfo(@PathVariable("idFlat") Integer idFlat, @PathVariable("idCounter") Integer idCounter, Model model, @ModelAttribute("indication") Indication indication) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Flat flat = flatService.getFlatById(idFlat).get();
        List<Flat> flats = new ArrayList<Flat>();
        if(user.getAuthority().equals("USER")) flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        model.addAttribute("flat", flat);
        model.addAttribute("counter", counterService.getCounterById(idCounter).get());
        model.addAttribute("periodIndication", Period.getPeriod());
        if (flats.contains(flat) || user.getAuthority().equals("ADMIN")) return "indication/create";
        else return "redirect:/Indication/create";
    }


    @PostMapping("{idFlat}/{idCounter}")
    public String createIndication(@PathVariable("idFlat") Integer idFlat, @PathVariable("idCounter") Integer idCounter, @ModelAttribute("indication") @Valid Indication indication, BindingResult bindingResult, Model model){
        Counter counter = counterService.getCounterById(idCounter).get();
        Flat flat = flatService.getFlatById(idFlat).get();
        String period = Period.getPeriod();
        model.addAttribute("flat", flat);
        model.addAttribute("counter", counter);
        model.addAttribute("periodIndication", period);
        if(indicationService.findAllByCounterAndPeriod(counter, period).size() != 0)
            bindingResult.addError(new FieldError("indication", "value", indication.getValue(),
                    false, null, null, "Показания по данному счетчику за данный период уже переданы, передача показания невозможна"));
        if(paymentService.findAllByFlatAndPeriod(flat, period).size() != 0)
            bindingResult.addError(new FieldError("indication", "value", indication.getValue(),
                    false, null, null, "Начисления за данный период уже произведены, передача показания невозможна"));
        if(bindingResult.hasErrors()) return "indication/create";
        indication.setCounter(counter);
        indication.setPeriod(period);
        indicationService.postIndication(indication);
        return "redirect:/Indication/list";
    }

    @GetMapping("/update/{id}")
    public String updatePage(Model model, @ModelAttribute("indication") Indication indication, @PathVariable("id") Integer id) {
        var indicationItem = indicationService.getIndicationById(id).get();
        model.addAttribute("indication", indicationItem);
        model.addAttribute("flat", indicationItem.getCounter().getFlat());
        model.addAttribute("counter", indicationItem.getCounter());
        model.addAttribute("periodIndication", Period.getPeriod());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "indication/update";
        else return "redirect:/Indication/list";
    }


    @PostMapping("/update/{id}")
    public String updateIndication(@ModelAttribute("indication") @Valid Indication indication, BindingResult bindingResult, Model model, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            model.addAttribute("counter", indication.getCounter());
            model.addAttribute("flat", indication.getCounter().getFlat());
            model.addAttribute("periodIndication", indication.getPeriod());

            if (paymentService.findAllByPeriod(Period.getPeriod()).size() != 0)
                bindingResult.addError(new FieldError("indication", "value", indication.getValue(),
                        false, null, null, "Начисления за данный период уже произведены, изменение невозможно"));
            if (bindingResult.hasErrors()) return "indication/update";
            indicationService.postIndication(indication);
        }
        return "redirect:/Indication/list";
    }

    @GetMapping("/delete/{id}")
    public String deletePage(Model model, @PathVariable("id") Integer id) {
        Indication indication = indicationService.getIndicationById(id).get();
        model.addAttribute("indication", indication);
        model.addAttribute("counter", indication.getCounter());
        model.addAttribute("flat", indication.getCounter().getFlat());
        model.addAttribute("periodIndication", indication.getPeriod() );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "indication/delete";
        else return "redirect:/Indication/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteIndication(Model model, @ModelAttribute("indication") Indication indication,  BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            var indicationItem = indicationService.getIndicationById(id).get();
            model.addAttribute("counter", indication.getCounter());
            model.addAttribute("flat", indication.getCounter().getFlat());
            model.addAttribute("periodIndication", indication.getPeriod());

            indication.setCounter(indicationItem.getCounter());
            indication.setPeriod(indicationItem.getPeriod());
            indication.setValue(indicationItem.getValue());

            if (paymentService.findAllByPeriod(indication.getPeriod()).size() != 0)
                bindingResult.addError(new FieldError("indication", "value", indication.getValue(),
                        false, null, null, "Удаление невозможно, так как показание задействовано в начислениях"));
            if (bindingResult.hasErrors()) return "indication/delete";
            indicationService.deleteIndication(id);
        }
        return "redirect:/Indication/list";
    }
}
