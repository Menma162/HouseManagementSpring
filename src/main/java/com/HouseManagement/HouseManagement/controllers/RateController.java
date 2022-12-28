package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Rate;
import com.HouseManagement.HouseManagement.models.User;
import com.HouseManagement.HouseManagement.services.RateService;
import com.HouseManagement.HouseManagement.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/Rate")
public class RateController {
    private final RateService rateService;
    private final UserService userService;

    public RateController(RateService rateService, UserService userService) {
        this.rateService = rateService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        List<Rate> rates = rateService.list();
        String role = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("rates", rates);
        return "rate/rates";
    }

    @GetMapping("/update/{id}")
    public String updatePage
            (Model model, @PathVariable("id") Integer id) {
        model.addAttribute("rate", rateService.getRateById(id).get());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
            return "rate/update";
        else return "redirect:/Rate/list";
    }

    @PostMapping("/update/{id}")
    public String updateRate(@ModelAttribute("rate") @Valid Rate rate, BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            Rate rateItem = rateService.getRateById(id).get();
            rate.setName(rateItem.getName());
            if (bindingResult.hasErrors()) return "rate/update";
            rateService.postRate(rate);
        }
        return "redirect:/Rate/list";
    }
}
