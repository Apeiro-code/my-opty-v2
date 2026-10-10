package com.myopty.shared.user;

import java.util.Collection;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The {@code app_user} row, and the identity the security filter chain sees at
 * the door.
 *
 * <p>One class deliberately plays both parts: the object the repository reads
 * from the database is the same object handed to Spring Security as the
 * principal, so there is no boundary where a "user" and a "UserDetails" could
 * disagree about role or status. The alternative — a translation layer — would
 * be more code to keep in sync than this entity is.
 *
 * <p>Field names follow the camelCase the schema already uses ({@code
 * passwordHash} → {@code password_hash}): Spring Data JDBC's default naming
 * strategy converts both ways, so no annotations are needed for anything except
 * the id.
 *
 * <p>Timestamps are absent on purpose. The columns carry
 * {@code DEFAULT CURRENT_TIMESTAMP}, so an insert that does not mention them is
 * still valid, and reading them back would buy nothing until some code has a
 * reason to care. The auditing config in {@code shared.config} remains the
 * right place to attach them when an entity needs them.
 */
public class AppUser implements UserDetails {

    @Id
    private final Long id;

    private final String email;
    private final String phone;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final AccountStatus status;
    private final boolean emailVerified;
    private final boolean phoneVerified;

    public AppUser(
            Long id,
            String email,
            String phone,
            String passwordHash,
            String fullName,
            Role role,
            AccountStatus status,
            boolean emailVerified,
            boolean phoneVerified) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Enabled means "may walk through the door", which is exactly
     * {@link AccountStatus#ACTIVE}. The other statuses are not expiry or lockout
     * — they are a decision about this account, so they all fold into the one
     * flag Spring Security checks before the password is even considered.
     */
    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
