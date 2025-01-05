/* (C)2024 */
package net.joostvdg.wwi.view;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import net.joostvdg.wwi.config.LdapConfig;
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

  private final LdapConfig ldapConfig;

  public SecurityConfiguration(LdapConfig ldapConfig) {
    this.ldapConfig = ldapConfig;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        auth -> auth.requestMatchers(new AntPathRequestMatcher("/login")).permitAll());
    super.configure(http);
    http.oauth2Login(
        httpSecurityOAuth2LoginConfigurer ->
            httpSecurityOAuth2LoginConfigurer.loginPage(LOGIN_URL).permitAll());

    if (ldapConfig.isLdapEnabled()) {
      http.formLogin(
          httpSecurityOAuth2LoginConfigurer ->
              httpSecurityOAuth2LoginConfigurer.loginPage(LOGIN_URL).permitAll());
    }

    setLoginView(http, LoginView.class);
  }

  @Autowired
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    logger.info("Configuring AuthenticationManagerBuilder");
    if (!ldapConfig.isLdapEnabled()) {
      logger.info("LDAP is not enabled, skipping configuration");
      return;
    }

    // TODO: move to Debug logging
    logger.info("LDAP is enabled, configuring LDAP authentication");
    logger.info("LDAP URL: {}", ldapConfig.getLdapUrl());
    logger.info("LDAP User DN Pattern: {}", ldapConfig.getLdapUserDnPattern());
    logger.info("LDAP User Search Base: {}", ldapConfig.getLdapUserSearchFilter());
    logger.info("LDAP Group Search Base: {}", ldapConfig.getLdapGroupSearchBase());
    logger.info("LDAP Group Role Attribute: {}", ldapConfig.getLdapGroupRoleAttribute());
    auth.ldapAuthentication()
        .userDnPatterns(ldapConfig.getLdapUserDnPattern())
        .userSearchFilter(ldapConfig.getLdapUserSearchFilter())
        .groupSearchBase(ldapConfig.getLdapGroupSearchBase())
        .groupRoleAttribute(ldapConfig.getLdapGroupRoleAttribute())
        .contextSource()
        .url(ldapConfig.getLdapUrl())
        .managerDn(ldapConfig.getLdapManagerDn())
        .managerPassword(ldapConfig.getLdapManagerPassword())
        .and()
        .userDetailsContextMapper(new InetOrgPersonContextMapper());
  }
}
