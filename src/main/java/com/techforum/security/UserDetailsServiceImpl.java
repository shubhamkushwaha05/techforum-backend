package com.techforum.security;

import com.techforum.entity.User;
import com.techforum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        // Support login by username OR email
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + usernameOrEmail));

        // FIX: Throw DisabledException for banned users instead of
        //      UsernameNotFoundException. Spring Security maps DisabledException
        //      to a clearer "User is disabled" message, and GlobalExceptionHandler
        //      now handles it with a proper 401 response.
        //      Previously, throwing UsernameNotFoundException caused Spring to
        //      wrap it as "Bad credentials" — losing the reason entirely.
        if (Boolean.TRUE.equals(user.getIsBanned())) {
            throw new DisabledException(
                    "Account has been banned. Contact support.");
        }

        // FIX: Also check isActive flag
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new DisabledException("Account is inactive.");
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}
