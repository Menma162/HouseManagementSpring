package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Tenant;
import com.HouseManagement.HouseManagement.models.User;
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
import java.util.Objects;

@Controller
@RequestMapping("/User")
public class UserController {
    private final UserService userService;
    private final TenantService tenantService;

    public UserController(UserService userService, TenantService tenantService) {
        this.userService = userService;
        this.tenantService = tenantService;
    }

    @GetMapping("/info")
    public String mainPage(Model model) {
        String role = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("user", user);
        return "user/info";
    }

    @GetMapping("/updateEmail")
    public String updatePageEmail (Model model) {
        String role = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("user", user);
        return "user/updateEmail";
    }

    @GetMapping("/updatePassword")
    public String updatePagePassword (Model model) {
        String role = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("user", user);
        return "user/updatePassword";
    }

    @PostMapping("/updateEmail")
    public String updateUserEmail(Model model, @ModelAttribute("user") @Valid User user, BindingResult bindingResult){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userItem = userService.getUserByName(authentication.getName()).get();
        Tenant tenant = new Tenant();
        if(userItem.getAuthority().equals("USER")) tenant = tenantService.getTenantsByEmail(userItem.getUsername()).get(0);
        if(user.getUsername() == null || Objects.equals(user.getUsername(), "")) bindingResult.addError(new FieldError("user", "username", user.getUsername(),
                false, null, null, "Необходимо заполнить поле"));
        if(tenantService.getTenantsByEmailNotId(tenant.getId(), user.getUsername()).size() != 0 ||
                userService.getUsersByEmailNotId(userItem.getId(), user.getUsername()).size() != 0)
            bindingResult.addError(new FieldError("user", "username", user.getUsername(),
                    false, null, null, "Такая почта уже занята"));
        model.addAttribute("user", user);
        if(bindingResult.hasErrors()) return "user/updateEmail";
        userItem.setUsername(user.getUsername());
        tenant.setEmail(user.getUsername());
        if (userItem.getAuthority().equals("USER")) tenantService.postTenant(tenant);
        userService.updateUser(userItem);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @PostMapping("/updatePassword")
    public String updateUserPassword(Model model, @ModelAttribute("user") @Valid User user, BindingResult bindingResult){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userItem = userService.getUserByName(authentication.getName()).get();
        model.addAttribute("user", user);
        if(bindingResult.hasErrors()) return "user/updatePassword";
        userItem.setPassword(user.getPassword());
        userService.updateUser(userItem);
        return "redirect:/User/info";
    }
}
