package com.ling.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// 自定义登录成功处理，返回 Json
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        Map<String,Object> resp = new HashMap<>();
        resp.put("status", 200);
        resp.put("msg", "登录成功");
        ObjectMapper om = new ObjectMapper();
        String s = om.writeValueAsString(resp);
        // 通过 HttpServletResponse 返回登录成功的 Json
        response.getWriter().write(s);
    }
}
