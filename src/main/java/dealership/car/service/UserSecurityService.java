package dealership.car.service;

import dealership.car.model.User;
import dealership.car.model.UserDetailsSecurity;
import dealership.car.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class UserSecurityService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<? extends UserDetails> getAllNonPredefinedUsers() {
        List<UserDetailsSecurity> result = new ArrayList<>();
        List<User> repResult = userRepository.findAllByNameIsNotIn(Arrays.asList("admin","system","kermit","gonzo","salon","fabryka"));
        if (repResult != null) {
            repResult.forEach(u -> result.add(new UserDetailsSecurity(u)));
        }
        return result;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userRepository.findByName(name);
        if (user == null) {
            throw new UsernameNotFoundException(name);
        }
        return new UserDetailsSecurity(user);
    }

    public boolean userExists(String name) {
        return userRepository.findByName(name) != null;
    }

    public void createUser(User userDetails) {
        try {
            loadUserByUsername(userDetails.getName());
        } catch (UsernameNotFoundException e) {
            userDetails.setActive(1);
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            userRepository.save(userDetails);
        }

    }
}