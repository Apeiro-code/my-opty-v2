package com.myopty.shared.auth;

import com.myopty.shared.user.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Turns an email address into the {@code app_user} row Spring Security
 * authenticates against.
 *
 * <p>Spring Security's stock {@code JdbcUserDetailsManager} expects an
 * {@code authorities} table; this project's role is a column on
 * {@code app_user}, so the manager is replaced rather than reshaped. A missing
 * account throws the normal {@link UsernameNotFoundException} — the
 * authentication provider hides it behind a generic failure, so probing the
 * endpoint cannot be used to discover which addresses are registered.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No app_user row for " + email));
    }
}
