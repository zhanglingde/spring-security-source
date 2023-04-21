package com.ling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()    // 开启权限配置
                // 所有请求都需要认证后才可访问
                .anyRequest().authenticated()
                .and()
                // 表单登录配置
                .formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/doLogin")
//                .defaultSuccessUrl("/index")
                .successHandler(new MyAuthenticationSuccessHandler())
//               重定向客户端跳转，不方便携带请求失败的异常信息（只能放在 URL 中）
//                .failureUrl("/mylogin.html")
//                .failureForwardUrl("/mylogin.html")
                .failureHandler(new MyAuthenticationFailureHandler())
                .usernameParameter("uname")
                .passwordParameter("passwd")
                .permitAll()
                .and()
                // 开启注销登录配置
                .logout()
                .logoutSuccessHandler(new MyLogoutSuccessHandler())
//                .logoutSuccessUrl("/mylogin.url")
                // 定义多个注销请求路径
//                .logoutRequestMatcher(
//                        new OrRequestMatcher(
//                                new AntPathRequestMatcher("/logout1","GET"),
//                                new AntPathRequestMatcher("logout2","POST")
//                        )
//                )
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessUrl("/mylogin.html")
                .and()
                // 禁用 csrf 防御
                .csrf().disable();
    }

    // 登录成功跳转配置
    SavedRequestAwareAuthenticationSuccessHandler successHandler(){
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/index");
        handler.setTargetUrlParameter("target");
        return handler;
    }

    // 登录失败处理配置
    SimpleUrlAuthenticationFailureHandler failureHandler(){
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler("/mylogin.html");
        // 登录失败后通过服务端跳转回登录页面
        handler.setUseForward(true);
        return handler;
    }
}
