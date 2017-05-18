package com.springuni.commons.security;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Created by lcsontos on 5/18/17.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfigurationSupport extends WebSecurityConfigurerAdapter {

  protected static final String LOGIN_ENDPOINT = "/session";

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
    return new JwtAuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler(
      JwtTokenService jwtTokenService, ObjectMapper objectMapper) {

    return new DefaultAuthenticationSuccessHandler(jwtTokenService, objectMapper);
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler(ObjectMapper objectMapper) {
    return new DefaultAuthenticationFailureHandler(objectMapper);
  }

  @Bean
  public JwtTokenService jwtTokenService() {
    return new JwtTokenServiceImpl();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    super.configure(auth);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint authenticationEntryPoint =
        (AuthenticationEntryPoint) getApplicationContext().getBean("authenticationEntryPoint");

    customizeRequestAuthorization(http.csrf().disable()
        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .sessionManagement().sessionCreationPolicy(STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/").permitAll()
        .antMatchers(POST, LOGIN_ENDPOINT).permitAll()
        .anyRequest().authenticated()
        .and());

    customizeFilters(
        http.addFilterBefore(
            createJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class));
  }

  protected Filter createJwtAuthenticationFilter() {
    JwtTokenService jwtTokenService =
        (JwtTokenService)getApplicationContext().getBean("jwtTokenService");
    return new JwtAuthenticationFilter(jwtTokenService);
  }

  protected void customizeFilters(HttpSecurity http) {
  }

  protected void customizeRequestAuthorization(HttpSecurity http) {
  }

}