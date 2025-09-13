package com.synapse.security.service;

import com.synapse.data.entity.User;
import com.synapse.data.repository.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * UserDetailsService implementation for Spring Security authentication.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndNotDeleted(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User account is inactive: " + username);
        }

        return new UserPrincipal(user);
    }

    /**
     * Custom UserDetails implementation.
     */
    public static class UserPrincipal implements UserDetails {
        private final User user;

        public UserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == User.UserStatus.ACTIVE && !user.getDeleted();
        }

        public User getUser() {
            return user;
        }
    }
}