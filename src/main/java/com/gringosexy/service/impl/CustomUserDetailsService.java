package com.gringosexy.service.impl;

import com.gringosexy.enums.UserStatus;
import com.gringosexy.model.User;
import com.gringosexy.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Find by username OR email
        User user = userRepository.findByEmailIgnoreCase(identifier)
                .or(() -> userRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con credenciales provistas: " + identifier));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new LockedException("Tu cuenta ha sido bloqueada permanentemente. Contacta a soporte.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new DisabledException("Tu cuenta se encuentra temporalmente suspendida.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new DisabledException("Tu cuenta está pendiente de activación por el Administrador. Realiza tu pago por WhatsApp para habilitarla.");
        }

        // Grant roles with ROLE_ prefix for Spring Security
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == UserStatus.ACTIVE,
                true,
                true,
                true,
                authorities
        );
    }
}
