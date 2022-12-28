package com.HouseManagement.HouseManagement.controllers;

import com.HouseManagement.HouseManagement.other.ConvertImage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;

@Controller
public class HomeController {
    @GetMapping("/")
    public String mainPage(Model model) throws IOException {
        String path = System.getProperty("user.dir") + "\\homePage\\home.png";
        model.addAttribute("photo", ConvertImage.toBase64(path));
        return "home/index";
    }
}
