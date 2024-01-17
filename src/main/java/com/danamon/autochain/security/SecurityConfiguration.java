package com.danamon.autochain.security;

import com.danamon.autochain.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPoint authEntryPoint;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(n -> n
                                .requestMatchers("/api/auth/logout").authenticated()
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/locations/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                                .anyRequest().permitAll() //kode to unclock security
                                .anyRequest().authenticated()
                )
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling.authenticationEntryPoint(authEntryPoint)
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
//                .logout(n -> n
//                        .logoutUrl("/api/auth/logout")
//                        .logoutSuccessHandler((request, response, authentication) -> {
//                            // Handle successful logout
//                            // e.g., redirect or send a response
//                            response.setStatus(HttpStatus.OK.value());
//                            response.getWriter().flush();
//                        })
//                        .invalidateHttpSession(true) // Invalidate session
//                        .deleteCookies("JSESSIONID")
//                );
        return http.build();
    }
}