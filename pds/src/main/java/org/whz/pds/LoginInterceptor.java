package org.whz.pds;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
@Component
public class LoginInterceptor implements HandlerInterceptor {
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         HttpSession session = request.getSession(false);
         if(session==null) {
             request.getRequestDispatcher("/login.html").forward(request,response);
             return false;
         }
         return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    }
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    }
}
