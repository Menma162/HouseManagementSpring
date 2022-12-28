package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Counter;
import com.HouseManagement.HouseManagement.models.Flat;
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
@RequestMapping("/Counter")
public class CounterController {
    private final CounterService counterService;
    private final IndicationService indicationService;
    private final UserService userService;
    private final TenantService tenantService;
    private final FlatService flatService;
    private final String[] types = new String[]{"Счетчик холодной воды", "Счетчик горячей воды", "Счетчик электрической энергии", "Газовый счетчик", "Счетчик отопления"};
    private final String[] statuses = new String[]{"Используется", "Не используется"};

    public CounterController(CounterService counterService, IndicationService indicationService, UserService userService, TenantService tenantService, FlatService flatService) {
        this.counterService = counterService;
        this.indicationService = indicationService;
        this.userService = userService;
        this.tenantService = tenantService;
        this.flatService = flatService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Counter> counters = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) counters = counterService.list();
        else
        {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                counters.addAll(counterService.findAllByFlat(flat.getId()));
            }
        }
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("counters", counters);
        model.addAttribute("filterSearchData", new FilterSearchData());
        model.addAttribute("types", types);
        model.addAttribute("statuses", statuses);
        return "counter/counters";
    }

    @GetMapping("/details/{id}")
    public String detailsPage(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Counter counter = counterService.getCounterById(id).get();
        List<Counter> counters = new ArrayList<Counter>();
        if(user.getAuthority().equals("USER")) {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                counters.addAll(counterService.findAllByFlat(flat.getId()));
            }
        }
        model.addAttribute("counter", counter);
        model.addAttribute("flat", flatService.getFlatById(counter.getFlat().getId()).get());
        var indication = indicationService.findByCounterAndPeriod(counter, Period.getPeriod());
        if (indication.isPresent()) model.addAttribute("indication", indication.get());
        else  model.addAttribute("indication", null);
        model.addAttribute("period",  Period.getPeriod());
        if (counters.contains(counter) || user.getAuthority().equals("ADMIN")) return "counter/details";
        else return "redirect:/Counter/list";
    }

    @PostMapping("/search")
    public String search(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Counter> counters = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) counters = counterService.list();
        else
        {
            var flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
            for (Flat flat:flats) {
                counters.addAll(counterService.findAllByFlat(flat.getId()));
            }
        }
        if(!Objects.equals(filterSearchData.getSearch(), "")) counters = counters.stream().filter(x -> x.getFlat().getFlatNumber().contains(filterSearchData.getSearch())).toList();
        if(!Objects.equals(filterSearchData.getType(), "${type}"))counters = counters.stream().filter(x -> x.getType().equals(filterSearchData.getType())).toList();
        boolean status = filterSearchData.getStatus().equals("Используется");
        if (!Objects.equals(filterSearchData.getStatus(), "${status}")) counters = counters.stream().filter(x -> x.isUsed() == status).toList();
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("counters",counters);
        model.addAttribute("types", types);
        model.addAttribute("statuses", statuses);
        return "counter/counters";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("flats", flatService.list());
        model.addAttribute("filterSearchData", new FilterSearchData());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "counter/selectFlat";
        else return "redirect:/Counter/list";
    }

    @PostMapping("/create")
    public String selectFlat(@ModelAttribute("flat") Flat flat){
        return "redirect:/Counter/" + flat.getId() + "/create";
    }

    @PostMapping("/create/search")
    public String searchFlat(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        List<Flat> flats = flatService.list();
        if(filterSearchData.getSearch() != null) flats = flats.stream().filter(x -> x.getFlatNumber().contains(filterSearchData.getSearch())).toList();
        model.addAttribute("flats",flats);
        return "counter/selectFlat";
    }

    @GetMapping("/{id}/create")
    public String createPageAddInfo(@PathVariable("id") Integer id, Model model, @ModelAttribute("counter") Counter counter) {
        model.addAttribute("flat", flatService.getFlatById(id).get());
        model.addAttribute("types", types);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "counter/create";
        else return "redirect:/Counter/list";
    }

    @PostMapping("{idFlat}/create")
    public String createCounter(@PathVariable("idFlat") Integer idFlat, @ModelAttribute("counter") @Valid Counter counter, BindingResult bindingResult, Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            model.addAttribute("flat", flatService.getFlatById(idFlat).get());
            model.addAttribute("types", types);
            if (counterService.findAllByTypeAndNumber(counter.getType(), counter.getNumber()).size() != 0)
                bindingResult.addError(new FieldError("counter", "number", counter.getNumber(),
                        false, null, null, "Уже существует счетчик такого типа с таким номером"));
            if (Objects.equals(counter.getType(), "${type}"))
                bindingResult.addError(new FieldError("counter", "type", null,
                        false, null, null, "Необходимо выбрать тип счетчика"));
            if (bindingResult.hasErrors()) return "counter/create";
            counter.setFlat(flatService.getFlatById(idFlat).get());
            counterService.postCounter(counter);
        }
        return "redirect:/Counter/list";
    }

    @GetMapping("/update/{id}")
    public String updatePage(Model model, @ModelAttribute("counter") Counter counter, @PathVariable("id") Integer id) {
        var counterItem = counterService.getCounterById(id).get();
        model.addAttribute("types", types);
        model.addAttribute("flat", counterItem.getFlat());
        model.addAttribute("counter", counterItem);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "counter/update";
        else return "redirect:/Counter/list";

    }


    @PostMapping("/update/{id}")
    public String updateCounter(@ModelAttribute("counter") @Valid Counter counter, BindingResult bindingResult, Model model, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            model.addAttribute("types", types);
            model.addAttribute("flat", counter.getFlat());
            if (counterService.findAllByTypeAndNumberIdNot(id, counter.getType(), counter.getNumber()).size() != 0)
                bindingResult.addError(new FieldError("counter", "number", counter.getNumber(),
                        false, null, null, "Уже существует счетчик такого типа с таким номером"));
            if (Objects.equals(counter.getType(), "${type}"))
                bindingResult.addError(new FieldError("counter", "type", null,
                        false, null, null, "Необходимо выбрать тип счетчика"));
            if (bindingResult.hasErrors()) return "counter/update";
            counterService.postCounter(counter);
        }
        return "redirect:/Counter/details/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deletePage(Model model, @PathVariable("id") Integer id) {
        var counterItem = counterService.getCounterById(id).get();
        model.addAttribute("types", types);
        model.addAttribute("flat", counterItem.getFlat());
        model.addAttribute("counter", counterItem);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "counter/delete";
        else return "redirect:/Counter/list";

    }

    @PostMapping("/delete/{id}")
    public String deleteCounter( Model model, @ModelAttribute("counter") Counter counter, BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            var counterItem = counterService.getCounterById(id).get();
            counter.setFlat(counterItem.getFlat());
            counter.setUsed(counterItem.isUsed());
            counter.setType(counterItem.getType());
            counter.setNumber(counterItem.getNumber());

            model.addAttribute("types", types);
            model.addAttribute("flat", counterItem.getFlat());

            if (indicationService.findAllByCounter(counter).size() != 0)
                bindingResult.addError(new FieldError("counter", "used", counter.isUsed(),
                        false, null, null, "Удаление невозможно, так как счетчик задействован в показаниях"));
            if (bindingResult.hasErrors()) return "counter/delete";
            counterService.deleteCounter(id);
        }
        return "redirect:/Counter/list";
    }
}
