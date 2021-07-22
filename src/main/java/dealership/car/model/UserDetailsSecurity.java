package dealership.car.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;

public class UserDetailsSecurity implements UserDetails {
    private User user;

    public UserDetailsSecurity() {
    }

    public UserDetailsSecurity(final User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user != null ? user.getRoles() : new HashSet<>();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user != null ? this.user.getName() : null;
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
        return this.user.getActive() > 0;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserDetailsSecurity [user=" + user + "]";
    }
}
