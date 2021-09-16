package dealership.car.camunda.model;

import dealership.car.model.RoleEnum;
import javax.validation.constraints.NotEmpty;

import java.util.HashSet;
import java.util.Set;

public class RegisterUserModel {

    @NotEmpty(message = "User is required.")
    private String user;

    @NotEmpty(message = "Password is required.")
    private String password;

    private Set<RoleEnum> roles;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<RoleEnum> getRoles() {
        if (roles == null)
            roles = new HashSet<>();

        return roles;
    }

    public void setRoles(Set<RoleEnum> roles) {
        this.roles = roles;
    }

}
