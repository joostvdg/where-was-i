/* (C)2024 */
package net.joostvdg.wwi.view;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

  private static final String LOGIN_URL = "/login";
  private final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        auth -> auth.requestMatchers(new AntPathRequestMatcher("/login")).permitAll());
    super.configure(http);
    http.oauth2Login(
        httpSecurityOAuth2LoginConfigurer ->
            httpSecurityOAuth2LoginConfigurer.loginPage(LOGIN_URL).permitAll());
    // http.oauth2Login().loginPage(LOGIN_URL).permitAll(); //Deprecated
    http.formLogin(
        httpSecurityOAuth2LoginConfigurer ->
            httpSecurityOAuth2LoginConfigurer.loginPage(LOGIN_URL).permitAll());
    // http.formLogin().loginPage(LOGIN_URL).permitAll();
    setLoginView(http, LoginView.class);
  }

  @Autowired
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    logger.info("Configuring AuthenticationManagerBuilder");
    auth.ldapAuthentication()
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
}
