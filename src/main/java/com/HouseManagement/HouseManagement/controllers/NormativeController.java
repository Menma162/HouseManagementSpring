package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.models.Normative;
import com.HouseManagement.HouseManagement.models.User;
import com.HouseManagement.HouseManagement.services.NormativeService;
import com.HouseManagement.HouseManagement.services.TenantService;
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
@RequestMapping("/Normative")
public class NormativeController {
    private final NormativeService normativeService;
    private final UserService userService;

    public NormativeController(NormativeService normativeService, UserService userService) {
        this.normativeService = normativeService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public String mainPage(Model model) {
        List<Normative> normatives = normativeService.list();
        String role = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN")) role = "ADMIN";
        model.addAttribute("role", role);
        model.addAttribute("normatives", normatives);
        return "normative/normatives";
    }

    @GetMapping("/update/{id}")
    public String updatePage
            (Model model, @PathVariable("id") Integer id) {
        model.addAttribute("normative", normativeService.getNormativeById(id).get());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByName(authentication.getName()).get();
        if(user.getAuthority().equals("ADMIN"))
             return "normative/update";
        else return "redirect:/Normative/list";

    }

    @PostMapping("/update/{id}")
    public String updateNormative(@ModelAttribute("normative") @Valid Normative normative, BindingResult bindingResult, @PathVariable("id") Integer id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAu = userService.getUserByName(authentication.getName()).get();
        if(userAu.getAuthority().equals("ADMIN")) {
            Normative normativeItem = normativeService.getNormativeById(id).get();
            normative.setName(normativeItem.getName());
            if (bindingResult.hasErrors()) return "normative/update";
            normativeService.postNormative(normative);
        }
        return "redirect:/Normative/list";
    }
}
