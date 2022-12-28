package com.HouseManagement.HouseManagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig  extends WebSecurityConfigurerAdapter {
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()

                .antMatchers("/Flat ").hasAnyRole("USER","ADMIN")
                .antMatchers("/Tenant").hasAnyRole("USER","ADMIN")
                .antMatchers("/Counter").hasAnyRole("USER","ADMIN")
                .antMatchers("/Indication").hasAnyRole("USER","ADMIN")
                .antMatchers("/Rate").hasAnyRole("USER","ADMIN")
                .antMatchers("/Normative").hasAnyRole("USER","ADMIN")
                .antMatchers("/Payment").hasAnyRole("USER","ADMIN")
                .antMatchers("/User").hasAnyRole("USER","ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .logout().permitAll();
    }
    @Autowired
    private DataSource dataSource;
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws
            Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
                .usersByUsernameQuery("select username,password,enabled from userstable where username=?")
                .authoritiesByUsernameQuery("select username,authority from userstable where username=?");
    }
}

