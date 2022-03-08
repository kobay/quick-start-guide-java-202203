package com.example.demo;

import com.box.sdk.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @Autowired
    HttpSession session;

    @GetMapping("/logout")
    public String register(Model model) {
        session.invalidate();
        return "login";
    }

}
