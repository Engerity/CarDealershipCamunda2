package dealership.car.config;

import dealership.car.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Klasa konfiguracji zabezpieczeń aplikacji.
 */
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserSecurityService userSecurityService;

    /**
     * Konfiguracja dostępu zasobów webowych.
     * @param http obiekt konfiguracji zabezpieczeń
     */
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

    /**
     * Konfiguracja autentykacji - wskazanie serwisu dostępu do użytkowników oraz rodzaju szyfrowania haseł.
     * @param auth obiekt konfiguracyjny
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userSecurityService).passwordEncoder(passwordEncoder());
    }

    /**
     * Rodzaj szyfrowania haseł.
     * @return obiekt szyfrujący
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
}
