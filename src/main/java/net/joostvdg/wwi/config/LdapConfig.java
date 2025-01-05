/* (C)2024 */
package net.joostvdg.wwi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapConfig {
  @Value("${ldap.enabled:false}")
  private boolean ldapEnabled;

  @Value("${ldap.url:}")
  private String ldapUrl;

  @Value("${ldap.base.dn:}")
  private String ldapBaseDn;

  @Value("${ldap.user.dn.pattern:}")
  private String ldapUserDnPattern;

  @Value("${ldap.manager.dn:}")
  private String ldapManagerDn;

  @Value("${ldap.manager.password:}")
  private String ldapManagerPassword;

  @Value("${ldap.user.dn:}")
  private String ldapUserDn;

  @Value("${ldap.user.search.filter:}")
  private String ldapUserSearchFilter;

  @Value("${ldap.group.dn:}")
  private String ldapGroupDn;

  @Value("${ldap.group.search.base:}")
  private String ldapGroupSearchBase;

  @Value("${ldap.group.role.attribute:}")
  private String ldapGroupRoleAttribute;

  public boolean isLdapEnabled() {
    return ldapEnabled;
  }

  public String getLdapUrl() {
    return ldapUrl;
  }

  public String getLdapBaseDn() {
    return ldapBaseDn;
  }

  public String getLdapUserDnPattern() {
    return ldapUserDnPattern;
  }

  public String getLdapManagerDn() {
    return ldapManagerDn;
  }

  public String getLdapManagerPassword() {
    return ldapManagerPassword;
  }

  public String getLdapUserDn() {
    return ldapUserDn;
  }

  public String getLdapUserSearchFilter() {
    return ldapUserSearchFilter;
  }

  public String getLdapGroupSearchBase() {
    return ldapGroupSearchBase;
  }

  public String getLdapGroupDn() {
    return ldapGroupDn;
  }

  public String getLdapGroupRoleAttribute() {
    return ldapGroupRoleAttribute;
  }
}
