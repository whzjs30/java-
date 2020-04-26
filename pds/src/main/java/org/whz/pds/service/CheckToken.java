package org.whz.pds.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CheckToken {

    public void checkTokne(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession(true);
        Object token = session.getAttribute("token");
        if(token==null){
            session.setAttribute("token", Math.random());
        }
    }
}
