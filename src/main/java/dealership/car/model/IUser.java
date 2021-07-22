package dealership.car.model;

import java.util.Collection;

public interface IUser {
    Long getId();

    String getName();

    Collection<? extends IRole> getRoles();
}
