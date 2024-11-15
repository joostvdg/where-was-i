package net.joostvdg.wwi.main;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.naming.directory.Attributes;
import java.util.Collection;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    private static final String LOGIN_URL = "/login";
    private final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/login"))
                .permitAll());
        super.configure(http);
        http.oauth2Login().loginPage(LOGIN_URL).permitAll();
        http.formLogin().loginPage(LOGIN_URL).permitAll();
        setLoginView(http, LoginView.class);

    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        logger.info("Configuring AuthenticationManagerBuilder");
        auth

            .ldapAuthentication()
                .userDnPatterns("ou=People")
                .userSearchFilter("uid={0}")
                .groupSearchBase("ou=Groups")
                .groupRoleAttribute("cn")
                .contextSource()
                    .url("ldap://localhost:389/dc=example,dc=org")
                    .managerDn("cn=admin,dc=example,dc=org")
                    .managerPassword("admin")
                .and()
                .userDetailsContextMapper(new InetOrgPersonContextMapper());
    }


//    @Bean(name = "userDetailsContextMapper", autowireCandidate = true)
//    public UserDetailsContextMapper userDetailsContextMapper() {
//        return new LdapUserDetailsMapper() {
//            @Override
//            public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
//                logger.info("Mapping user from context");
//                Attributes attributes = ctx.getAttributes();
//                logger.info("Attributes: {}", attributes);
//                return super.mapUserFromContext(ctx, username, authorities);
//            }
//        };
//    }
}