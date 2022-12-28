package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Flat;
import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.models.User;
import com.HouseManagement.HouseManagement.other.FilterSearchData;
import com.HouseManagement.HouseManagement.services.FlatService;
import com.HouseManagement.HouseManagement.services.TenantService;
import com.HouseManagement.HouseManagement.services.UserService;
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
@RequestMapping("/Tenant")
public class TenantController {
    private final TenantService tenantService;
    private final FlatService flatService;
    private final UserService userService;

    public TenantController(TenantService tenantService, FlatService flatService, UserService userService) {
        this.tenantService = tenantService;
        this.flatService = flatService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Tenant> tenants = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) tenants = tenantService.list();
        else tenants = tenantService.getTenantsByEmail(authentication.getName());
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("tenants", tenants);
        model.addAttribute("filterSearchData", new FilterSearchData());
        return "tenant/tenants";
    }

    @GetMapping("/details/{id}")
    public String detailsPage(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        Tenant tenant = tenantService.getTenantById(id).get();
        List<Tenant> tenants = new ArrayList<Tenant>();
        if(user.getAuthority().equals("USER")) tenants = tenantService.getTenantsByEmail(authentication.getName());
        model.addAttribute("tenant", tenant);
        model.addAttribute("flats", flatService.listByTenant(tenantService.getTenantById(id).get()));
        if (tenants.contains(tenant) || user.getAuthority().equals("ADMIN")) return "tenant/details";
        else return "redirect:/Tenant/list";
    }

    @PostMapping("/search")
    public String searchFlat(Model model, @ModelAttribute("filterSearchData") FilterSearchData filterSearchData){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        List<Tenant> tenants = new ArrayList<>();
        if(user.getAuthority().equals("ADMIN")) tenants = tenantService.list();
        else tenants = tenantService.getTenantsByEmail(authentication.getName());
        if(filterSearchData.getSearch() != null) tenants = tenants.stream().filter(x -> x.getFullName().contains(filterSearchData.getSearch())).toList();
        model.addAttribute("tenants",tenants);
        String role = null;
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        return "tenant/tenants";
    }

    @GetMapping("/create")
    public String createPage(@ModelAttribute("tenant") Tenant tenant) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "tenant/create";
        else return "redirect:/Tenant/list";
    }

    @PostMapping("/create")
    public String createTenant(@ModelAttribute("tenant") @Valid Tenant tenant, BindingResult bindingResult){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) {
            if (tenantService.getTenantsByEmail(tenant.getEmail()).size() != 0)
                bindingResult.addError(new FieldError("tenant", "email", tenant.getEmail(),
                        false, null, null, "Квартиросъемщик с такой электронной почтой уже существует"));
            if (tenantService.findAllByPhoneNumber(tenant.getPhoneNumber()).size() != 0)
                bindingResult.addError(new FieldError("tenant", "phoneNumber", tenant.getPhoneNumber(),
                        false, null, null, "Квартиросъемщик с таким номером телефона уже существует"));
            if (bindingResult.hasErrors()) return "tenant/create";
            tenantService.postTenant(tenant);
        }
        return "redirect:/Tenant/list";
    }

    @GetMapping("/update/{id}")
    public String updatePage
            (Model model, @PathVariable("id") Integer id) {
        model.addAttribute("tenant", tenantService.getTenantById(id).get());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "tenant/update";
        else return "redirect:/Tenant/list";
    }

    @PostMapping("/update/{id}")
    public String updateTenant(@ModelAttribute("tenant") @Valid Tenant tenant, BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) {
            var tenantItem = tenantService.getTenantById(id);
            var userItem = userService.getUserByName(tenantItem.get().getEmail());
            if (tenantService.getTenantsByEmailNotId(id, tenant.getEmail()).size() != 0 ||
                    (userItem.isPresent() && userService.getUsersByEmailNotId(userItem.get().getId(), tenant.getEmail()).size() != 0))
                bindingResult.addError(new FieldError("tenant", "email", tenant.getEmail(),
                        false, null, null, "Электронная почта уже занята"));
            if (tenantService.getTenantsByPhoneNumberNotId(id, tenant.getPhoneNumber()).size() != 0)
                bindingResult.addError(new FieldError("tenant", "phoneNumber", tenant.getPhoneNumber(),
                        false, null, null, "Квартиросъемщик с таким номером телефона уже существует"));
            if (bindingResult.hasErrors()) return "tenant/update";
            if (userItem.isPresent()) {
                userItem.get().setUsername(tenant.getEmail());
                userService.updateUser(userItem.get());
            }
            tenantService.postTenant(tenant);
        }
        return "redirect:/Tenant/details/" + id;
    }

    @GetMapping("/account/{id}")
    public String updateUserPage(Model model, @ModelAttribute("user") @Valid User user, @PathVariable("id") Integer id){
        Tenant tenant = tenantService.getTenantById(id).get();
        model.addAttribute("tenant", tenant);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN"))
            return "tenant/account";
        else return "redirect:/Tenant/list";
    }

    @PostMapping("/account/{id}")
    public String updateUser(Model model, @ModelAttribute("user") @Valid User user, BindingResult bindingResult, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            Tenant tenant = tenantService.getTenantById(id).get();
            if (user.getUsername() == null || Objects.equals(user.getUsername(), ""))
                bindingResult.addError(new FieldError("user", "username", user.getUsername(),
                        false, null, null, "Необходимо заполнить поле"));
            var userItem = userService.getUserByName(tenant.getEmail());
            if ((userItem.isPresent() && userService.getUsersByEmailNotId(userItem.get().getId(), user.getUsername()).size() != 0) ||
                    tenantService.getTenantsByEmailNotId(tenant.getId(), user.getUsername()).size() != 0 ||
                    !userItem.isPresent() && userService.getUserByName(user.getUsername()).isPresent())
                bindingResult.addError(new FieldError("user", "username", user.getUsername(),
                        false, null, null, "Такая почта уже занята"));
            model.addAttribute("user", user);
            model.addAttribute("tenant", tenant);
            if (bindingResult.hasErrors()) return "tenant/account";
            if (userItem.isPresent()) user.setId(userItem.get().getId());
            user.setAuthority("USER");
            user.setEnabled(true);
            tenant.setEmail(user.getUsername());
            tenantService.postTenant(tenant);
            userService.updateUser(user);
        }
        return "redirect:/Tenant/list";
    }

    @GetMapping("/delete/{id}")
    public String deletePage
            (Model model, @PathVariable("id") Integer id) {
        model.addAttribute("tenant", tenantService.getTenantById(id).get());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN"))
            return "tenant/delete";
        else return "redirect:/Tenant/list";

    }

    @PostMapping("/delete/{id}")
    public String deleteTenant(Model model, @ModelAttribute("tenant") Tenant tenant,  BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) {
            Tenant tenantItem = tenantService.getTenantById(id).get();
            tenant.setFullName(tenantItem.getFullName());
            tenant.setDateOfRegistration(tenantItem.getDateOfRegistration());
            tenant.setEmail(tenantItem.getEmail());
            tenant.setId(tenantItem.getId());
            tenant.setPhoneNumber(tenantItem.getPhoneNumber());
            tenant.setNumberOfFamilyMembers(tenantItem.getNumberOfFamilyMembers());
            model.addAttribute("tenant", tenant);
            if (tenantService.findAllByTenant(tenant.getId()).size() != 0)
                bindingResult.addError(new FieldError("tenant", "email", tenant.getEmail(),
                        false, null, null, "Удаление невозможно, так как квартиросъемщик задействован в квартирах"));
            if (bindingResult.hasErrors()) return "tenant/delete";
            var userItem = userService.getUserByName(tenantItem.getEmail());
            if (userItem.isPresent()) userService.deleteUser(userItem.get().getId());
            tenantService.deleteTenant(tenant);
        }
        return "redirect:/Tenant/list";
    }
}
