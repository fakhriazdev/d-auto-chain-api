package com.danamon.autochain.security;

import com.danamon.autochain.service.AuthService;
import com.danamon.autochain.service.BackOfficeService;
import com.danamon.autochain.service.UserService;
import com.danamon.autochain.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@Lazy
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Override // dipanggil saat sebelum controller di hit
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // mengambil token yang ada di header
            String headerAuth = request.getHeader("Authorization");
            String token = null;

            // filter header authorization menjadi token
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                token = headerAuth.substring(7);
            }

            // jika token tidak null & token valid in memorry database
            if (token != null && jwtUtil.verifyJwtToken(token)) {

                String requestURI = request.getRequestURI();
                String actor = "";
                if (requestURI.contains("backoffice")) actor = "backoffice";
                if (requestURI.contains("user")) actor = "user";

                // set authentication ke spring security
                Map<String, String> userInfo = jwtUtil.getUserInfoByToken(token);

                UserDetails user = authService.loadToken(userInfo.get("userId"), actor);

                System.out.println(userInfo);
                System.out.println(user.getAuthorities());
                System.out.println(user.getUsername());
                System.out.println(user);

                // validasi/authentication by token
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                // menambahkan informasi tambahan berupa alamat IP Address, Host ke bentuk spring security
                authenticationToken.setDetails(new WebAuthenticationDetails(request));

                // menyimpan authentication ke spring security context
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        // ini gunanya untuk melanjutkan filter ke controller/filter lain
        filterChain.doFilter(request, response);
    }
}