/*
 * Copyright (c) 2017, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.security.boot.pac4j;

import org.pac4j.core.client.Clients;
import org.pac4j.springframework.security.authentication.ClientAuthenticationProvider;
import org.pac4j.springframework.security.web.ClientAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true, securedEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    ApplicationContext context;

    @Autowired
    Clients clients;

    @Autowired
    ClientAuthenticationProvider clientProvider;

    @Override
    public void configure(WebSecurity web) throws Exception {
         web
         .ignoring()
         .antMatchers(
             "/**/*.css",
             "/**/*.png",
             "/**/*.gif",
             "/**/*.jpg",
             "/**/*.ico",
             "/**/*.js"
         );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable()
        .authorizeRequests()
            .and()
        .formLogin()
            .loginPage("/login")
            .permitAll()
            .and()
        .logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .permitAll()
         ;

        http.addFilterBefore(clientFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);

        auth.authenticationProvider(clientProvider);
    }

    ClientAuthenticationFilter clientFilter() {

        String suffixUrl="/*";

        ClientAuthenticationFilter clientAuthenticationFilter = new ClientAuthenticationFilter(suffixUrl);
        clientAuthenticationFilter.setClients(clients);
        clientAuthenticationFilter.setSessionAuthenticationStrategy(sas());
        //clientAuthenticationFilter.setAuthenticationManager((AuthenticationManager)clientProvider);

        return clientAuthenticationFilter;
        /*
        return new ClientAuthenticationFilter(
                clients: clients,
                sessionAuthenticationStrategy: sas(),
                authenticationManager: clientProvider as AuthenticationManager
        )
        */
    }

    @Bean
    SessionAuthenticationStrategy sas() {
        return new SessionFixationProtectionStrategy();
    }

}