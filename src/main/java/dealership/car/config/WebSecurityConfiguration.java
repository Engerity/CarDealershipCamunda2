package dealership.car.config;

import dealership.car.camunda.service.CamundaProcessService;
import dealership.car.model.RoleEnum;
import dealership.car.model.User;
import dealership.car.repository.UserRepository;
import dealership.car.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CamundaProcessService camundaProcessService;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .headers()
                .frameOptions().disable() // Required for H2 console
                .and()
                .csrf()
                .disable() // CSRF protection is not supported by Camunda Webapps
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login?error")
                .and()
                .authorizeRequests()
                .antMatchers("/login", "/register/**").permitAll()
                .antMatchers("/app/**","/camunda/**", "/lib/**", "/api/**", "/engine-rest/**").permitAll() // Required for Camunda Webapps
                .antMatchers("/h2-console/**").permitAll() // Required for H2 console
                .antMatchers("/webjars/**", "/js/**").permitAll()
                .antMatchers("/img/**", "/css/**").permitAll() // Static resources
                .anyRequest().authenticated();
        // @formatter:on
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(setup()).passwordEncoder(passwordEncoder());
    }

    @Bean @Transactional
    public UserDetailsService setup() {
        User admin = userRepository.findByName("admin");
        if (admin == null) {
            admin = new User();
            admin.setActive(1);
            admin.setName("admin");
            admin.setPassword(passwordEncoder().encode("admin"));
            admin.getRoles().addAll(Arrays.asList(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_CLIENT, RoleEnum.ROLE_DEALERSHIP, RoleEnum.ROLE_FACTORY_WORKER));
            userRepository.save(admin);
        }
        User system = userRepository.findByName("system");
        if (system == null) {
            system = new User();
            system.setActive(1);
            system.setName("system");
            system.setPassword(passwordEncoder().encode("system"));
            system.getRoles().addAll(Arrays.asList(RoleEnum.ROLE_CLIENT, RoleEnum.ROLE_DEALERSHIP, RoleEnum.ROLE_FACTORY_WORKER));
            userRepository.save(system);
        }
        User kermit = userRepository.findByName("kermit");
        if (kermit == null) {
            kermit = new User();
            kermit.setActive(1);
            kermit.setName("kermit");
            kermit.setPassword(passwordEncoder().encode("kermit"));
            kermit.getRoles().add(RoleEnum.ROLE_CLIENT);
            userRepository.save(kermit);
        }
        User gonzo = userRepository.findByName("gonzo");
        if (gonzo == null) {
            gonzo = new User();
            gonzo.setActive(1);
            gonzo.setName("gonzo");
            gonzo.setPassword(passwordEncoder().encode("gonzo"));
            gonzo.getRoles().add(RoleEnum.ROLE_CLIENT);
            userRepository.save(gonzo);
        }
        User salon = userRepository.findByName("salon");
        if (salon == null) {
            salon = new User();
            salon.setActive(1);
            salon.setName("salon");
            salon.setPassword(passwordEncoder().encode("salon"));
            salon.getRoles().add(RoleEnum.ROLE_DEALERSHIP);
            userRepository.save(salon);
        }
        User fabryka = userRepository.findByName("fabryka");
        if (fabryka == null) {
            fabryka = new User();
            fabryka.setActive(1);
            fabryka.setName("fabryka");
            fabryka.setPassword(passwordEncoder().encode("fabryka"));
            fabryka.getRoles().add(RoleEnum.ROLE_FACTORY_WORKER);
            userRepository.save(fabryka);
        }
        camundaProcessService.initialGroupConfig(userRepository.findAll());

        return userSecurityService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
}
