<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%><% 
    
    if(!com.mothsoft.alexis.security.CurrentUserUtil.isAuthenticated()) {
        response.sendRedirect("/alexis/login/");
    } else {
        response.sendRedirect("/alexis/dashboard/");
    }
%>    
