package dealership.car.model;

import org.springframework.security.core.GrantedAuthority;

public enum RoleEnum implements GrantedAuthority, IRole {
    ROLE_CLIENT(IRoleValue.ROLE_CLIENT, "Klient/Użytkownik"),

    ROLE_DEALERSHIP(IRoleValue.ROLE_DEALERSHIP, "Pracownik salonu"),

    ROLE_FACTORY_WORKER(IRoleValue.ROLE_FACTORY_WORKER, "Pracownik fabryki"),

    ROLE_ADMIN(IRoleValue.ROLE_ADMIN, "Administrator");

    private final String value;

    private final String label;

    RoleEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getAuthority() {
        return getValue();
    }

    public static RoleEnum byIRole(IRole iRole) {
        if (iRole != null) {
            for (RoleEnum r : values()) {
                if (r.getValue().equals(iRole.getValue()))
                    return r;
            }
        }
        return null;
    }
}