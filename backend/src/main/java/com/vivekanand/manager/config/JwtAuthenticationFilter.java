
package com.vivekanand.manager.config;

import com.vivekanand.manager.auth.JwtUtil;
import com.vivekanand.manager.auth.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parse(token).getBody();
                String username = claims.getSubject();
                var userOpt = userRepo.findByUsername(username);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    var auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.authorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) { /* invalid token -> ignore */ }
        }
        chain.doFilter(request, response);
    }
}
