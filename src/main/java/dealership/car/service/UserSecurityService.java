package dealership.car.service;

import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.model.RoleEnum;
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

    @Autowired
    private CamundaProcessService camundaProcessService;


    public List<? extends UserDetails> getAllNonPredefinedUsers() {
        List<UserDetailsSecurity> result = new ArrayList<>();
        List<User> repResult = userRepository.findAllByNameIsNotIn(Arrays.asList("admin","system","kermit","gonzo","salon","fabryka"));
        if (repResult != null) {
            repResult.forEach(u -> result.add(new UserDetailsSecurity(u)));
        }
        return result;
    }

    /**
     * Wyszukuje użytkownika o podanej nazwie.
     * @param name nazwa użytkownika
     * @return użytkownik
     * @throws UsernameNotFoundException w przypadku braku użytkownika w bazie danych
     */
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

    /**
     * Tworzy nowego użytkownika w bazie danych.
     * @param userDetails użytkownik do zapisania w bazie danych
     */
    public void createUser(User userDetails) {
        try {
            loadUserByUsername(userDetails.getName());
        } catch (UsernameNotFoundException e) {
            userDetails.setActive(1);
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            userRepository.save(userDetails);
        }

    }

    /**
     * Inicjalizuje predefiniowanych użytkowników w systemie, przyisane do nich role oraz grupy w Camunda.
     */
    public void setup() {
        User admin = userRepository.findByName("admin");
        if (admin == null) {
            admin = new User();
            admin.setActive(1);
            admin.setName("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.getRoles().addAll(Arrays.asList(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_CLIENT, RoleEnum.ROLE_DEALERSHIP, RoleEnum.ROLE_FACTORY_WORKER));
            userRepository.save(admin);
        }
        User system = userRepository.findByName("system");
        if (system == null) {
            system = new User();
            system.setActive(1);
            system.setName("system");
            system.setPassword(passwordEncoder.encode("system"));
            system.getRoles().addAll(Arrays.asList(RoleEnum.ROLE_CLIENT, RoleEnum.ROLE_DEALERSHIP, RoleEnum.ROLE_FACTORY_WORKER));
            userRepository.save(system);
        }
        User kermit = userRepository.findByName("kermit");
        if (kermit == null) {
            kermit = new User();
            kermit.setActive(1);
            kermit.setName("kermit");
            kermit.setPassword(passwordEncoder.encode("kermit"));
            kermit.getRoles().add(RoleEnum.ROLE_CLIENT);
            userRepository.save(kermit);
        }
        User gonzo = userRepository.findByName("gonzo");
        if (gonzo == null) {
            gonzo = new User();
            gonzo.setActive(1);
            gonzo.setName("gonzo");
            gonzo.setPassword(passwordEncoder.encode("gonzo"));
            gonzo.getRoles().add(RoleEnum.ROLE_CLIENT);
            userRepository.save(gonzo);
        }
        User salon = userRepository.findByName("salon");
        if (salon == null) {
            salon = new User();
            salon.setActive(1);
            salon.setName("salon");
            salon.setPassword(passwordEncoder.encode("salon"));
            salon.getRoles().add(RoleEnum.ROLE_DEALERSHIP);
            userRepository.save(salon);
        }
        User fabryka = userRepository.findByName("fabryka");
        if (fabryka == null) {
            fabryka = new User();
            fabryka.setActive(1);
            fabryka.setName("fabryka");
            fabryka.setPassword(passwordEncoder.encode("fabryka"));
            fabryka.getRoles().add(RoleEnum.ROLE_FACTORY_WORKER);
            userRepository.save(fabryka);
        }
        camundaProcessService.initialGroupConfig(userRepository.findAll());
    }
}