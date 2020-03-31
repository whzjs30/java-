package org.whz.webshell.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.whz.webshell.service.SshService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class mainController {
    @Autowired
    private SshService sshService;
    @GetMapping("/v1/connection")
    @ResponseBody
    public Map entry(HttpServletRequest request, HttpServletResponse response){

        ArrayList<String> cmdlist = new ArrayList<>();
        Map<String, List<String>> resultList = sshService.execCMD(request,  response);
        return resultList;
    }
    @GetMapping("/v1")
    public String index(HttpServletRequest request, HttpServletResponse response){
        return "html/index";
    }

    @GetMapping("/v1/reconnection")
    @ResponseBody
    public String reconnection(HttpServletRequest request, HttpServletResponse response){

        sshService.invalidSession(request,  response);
        return "success";
    }
}
