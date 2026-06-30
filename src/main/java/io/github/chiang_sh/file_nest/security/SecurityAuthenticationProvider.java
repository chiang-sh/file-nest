package io.github.chiang_sh.file_nest.security;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

public class SecurityAuthenticationProvider implements AuthenticationProvider {

    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;

    public SecurityAuthenticationProvider(SecurityService securityService, PasswordEncoder passwordEncoder) {
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @NullMarked
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = Objects.toString(authentication.getCredentials(), "");
        UserDetails user = securityService.loadUserByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } else {
            throw new BadCredentialsException("Invalid Password.");
        }
    }

    @Override
    @NullMarked
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
