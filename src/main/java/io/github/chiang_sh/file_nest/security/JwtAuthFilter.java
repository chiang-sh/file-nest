package io.github.chiang_sh.file_nest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecurityService securityService;
    private final JwtUtils jwtUtils;

    @Autowired
    public JwtAuthFilter(SecurityService securityService, JwtUtils jwtUtils) {
        this.securityService = securityService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        jwtUtils.getRequestToken(request)
                .ifPresent(
                        token -> {
                            if (jwtUtils.validateToken(token)) {
                                String username = jwtUtils.getUsernameFromToken(token);
                                UserDetails user = securityService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user, null, user.getAuthorities());
                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);
                            }
                        });
        filterChain.doFilter(request, response);
    }
}
