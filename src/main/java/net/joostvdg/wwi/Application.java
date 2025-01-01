/* (C)2024 */
package net.joostvdg.wwi;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import java.io.Serial;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.modulith.Modulithic;

/**
 * The entry point of the Spring Boot application.
 *
 * <p>Use the @PWA annotation make the application installable on phones, tablets and some desktop
 * browsers.
 */
@SpringBootApplication(exclude = LdapAutoConfiguration.class)
@PWA(name = "Project Base for Vaadin with Spring", shortName = "Project Base")
@Theme(value = "my-theme", variant = Lumo.DARK)
@Modulithic(systemName = "Where Was I?")
public class Application implements AppShellConfigurator {

  @Serial private static final long serialVersionUID = 1L;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
