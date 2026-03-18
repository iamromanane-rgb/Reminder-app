package com.reminder.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.reminder.backend.models.AccessLevel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter { //happens only once every api call

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");//expects bearer token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); //cuts off the bearer part to get only the token
            if (jwtService.isTokenValid(token)) { //calls jwtservice to check the validity of token with sign
                String subject = jwtService.getSubject(token);//if valid we take the data from token (email)
                AccessLevel accessLevel = jwtService.getAccessLevel(token);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ACCESS_READ"));
                if (accessLevel == AccessLevel.READ_WRITE) {
                    authorities.add(new SimpleGrantedAuthority("ACCESS_READ_WRITE"));
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
