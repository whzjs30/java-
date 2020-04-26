package org.whz.pds.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class MainController {
    @RequestMapping("/check")
    public String login(@RequestParam("user")String user, @RequestParam("psd")String psd, HttpServletRequest request, HttpServletResponse response) {
        if (user.equals("root") && psd.equals("root")) {
            HttpSession session = request.getSession(true);
            String token = Double.toString(Math.random());
            session.setAttribute("token", token);
            response.addCookie(new Cookie("token",token));
        }
        return "index.html";
    }
    @RequestMapping("/login")
    public String index(){
        return "login.html";
    }
}
