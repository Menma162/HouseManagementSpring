package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.models.User;
import com.HouseManagement.HouseManagement.other.FilterSearchData;
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

@Controller
@RequestMapping("/Flat")
public class FlatController {
    private final FlatService flatService;
    private final TenantService tenantService;
    private final CounterService counterService;
    private final PaymentService paymentService;
    private final UserService userService;

    public FlatController(FlatService flatService, TenantService tenantService, CounterService counterService, PaymentService paymentService, UserService userService) {
        this.flatService = flatService;
        this.tenantService = tenantService;
        this.counterService = counterService;
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Flat> flats = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) flats = flatService.list();
        else flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("flats", flats);
        model.addAttribute("filterSearchData", new FilterSearchData());
        return "flat/flats";
    }

    @GetMapping("/details/{id}")
    public String detailsPage(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Flat flat = flatService.getFlatById(id).get();
        List<Flat> flats = new ArrayList<Flat>();
        if(user.getAuthority().equals("USER")) flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        model.addAttribute("flat", flat);
        if (flats.contains(flat) || user.getAuthority().equals("ADMIN")) return "flat/details";
        else return "redirect:/Flat/list";
    }

    @PostMapping("/search")
    public String searchFlat(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Flat> flats = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) flats = flatService.list();
        else flats = flatService.listByTenant(tenantService.getTenantsByEmail(authentication.getName()).get(0));
        if(filterSearchData.getSearch() != null) flats = flats.stream().filter(x -> x.getFlatNumber().contains(filterSearchData.getSearch())).toList();
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("flats",flats);
        return "flat/flats";
    }

    @GetMapping("/create")
    public String createPage(Model model, @ModelAttribute("flat") Flat flat) {
        model.addAttribute("tenantsList",tenantService.list());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "flat/create";
        else return "redirect:/Flat/list";
    }


    @PostMapping("/create")
    public String createFlat(Model model, @ModelAttribute("flat") @Valid Flat flat, BindingResult bindingResult){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")){
            model.addAttribute("tenantsList",tenantService.list());
            if(flatService.getFlatsByPhoneNumber(flat.getFlatNumber()).size() != 0)
                bindingResult.addError(new FieldError("flat", "flatNumber", flat.getFlatNumber(),
                        false, null, null, "Квартира с таким номером уже существует"));
            if(flatService.getFlatsByPersonalAccount(flat.getPersonalAccount()).size() != 0)
                bindingResult.addError(new FieldError("flat", "personalAccount", flat.getPersonalAccount(),
                        false, null, null, "Квартира с таким лицевым счетом уже существует"));
            if((flat.getTotal() != null && flat.getUsablea() != null) && flat.getTotal() < flat.getUsablea())
                bindingResult.addError(new FieldError("flat", "total", flat.getTotal(),
                        false, null, null, "Общая площадь должна быть больше полезной"));
            if(bindingResult.hasErrors()) return "flat/create";
            flatService.postFlat(flat);
        }
        return "redirect:/Flat/list";
    }

    @GetMapping("/update/{id}")
    public String updatePage(Model model, @ModelAttribute("flat") Flat flat, @PathVariable("id") Integer id) {
        model.addAttribute("tenantsList",tenantService.list());
        model.addAttribute("flat", flatService.getFlatById(id).get());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "flat/update";
        else return "redirect:/Flat/list";
    }


    @PostMapping("/update/{id}")
    public String updateFlat(@ModelAttribute("flat") @Valid Flat flat, BindingResult bindingResult, Model model,  @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) {
            model.addAttribute("tenantsList", tenantService.list());
            if (flatService.getFlatsByNumberNotId(id, flat.getFlatNumber()).size() != 0)
                bindingResult.addError(new FieldError("flat", "flatNumber", flat.getFlatNumber(),
                        false, null, null, "Квартира с таким номером уже существует"));
            if (flatService.getFlatsByPersonalAccountNotId(id, flat.getPersonalAccount()).size() != 0)
                bindingResult.addError(new FieldError("flat", "personalAccount", flat.getPersonalAccount(),
                        false, null, null, "Квартира с таким лицевым счетом уже существует"));
            if ((flat.getTotal() != null && flat.getUsablea() != null) && flat.getTotal() < flat.getUsablea())
                bindingResult.addError(new FieldError("flat", "total", flat.getTotal(),
                        false, null, null, "Общая площадь должна быть больше полезной"));
            if (bindingResult.hasErrors()) return "flat/update";
            flatService.postFlat(flat);
        }
        return "redirect:/Flat/details/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deletePage(Model model, @PathVariable("id") Integer id) {
        model.addAttribute("tenantsList",tenantService.list());
        model.addAttribute("flat", flatService.getFlatById(id).get());
        Tenant tenant = flatService.getFlatById(id).get().getTenant();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "flat/delete";
        else return "redirect:/Flat/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteFlat(Model model, @ModelAttribute("flat") Flat flat,  BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) {
            Flat flatItem = flatService.getFlatById(flat.getId()).get();
            flat.setFlatNumber(flatItem.getFlatNumber());
            flat.setTenant(flatItem.getTenant());
            flat.setEntranceNumber(flatItem.getEntranceNumber());
            flat.setNumberOfOwners(flatItem.getNumberOfOwners());
            flat.setNumberOfRooms(flatItem.getNumberOfRooms());
            flat.setNumberOfRegisteredResidents(flatItem.getNumberOfRegisteredResidents());
            flat.setUsablea(flatItem.getUsablea());
            flat.setTotal(flatItem.getTotal());
            flat.setPersonalAccount(flatItem.getPersonalAccount());
            model.addAttribute("tenantsList", tenantService.list());
            if (counterService.findAllByFlat(id).size() != 0 || paymentService.findAllByFlat(flat.getId()).size() != 0)
                bindingResult.addError(new FieldError("flat", "tenant", flat.getTenant(),
                        false, null, null, "Удаление невозможно, так как квартира задействована в счетчиках или начислениях"));
            if (bindingResult.hasErrors()) return "flat/delete";
            flatService.deleteFlat(flat);
        }
        return "redirect:/Flat/list";
    }
}
