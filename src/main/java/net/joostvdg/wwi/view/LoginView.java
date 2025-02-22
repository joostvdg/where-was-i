/* (C)2024 */
package net.joostvdg.wwi.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.io.Serial;
import net.joostvdg.wwi.config.LdapConfig;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

  @Serial private static final long serialVersionUID = 1L;

  // URL that Spring Security uses to connect to Google services
  private static final String OAUTH_2_AUTHORIZATION_GITHUB = "/oauth2/authorization/github";

  public LoginView(LdapConfig ldapConfig) {
    // Title for the login page
    H1 title = new H1("Login Page");

    // GitHub Login Button
    Button githubButton =
        new Button(
            "Login with GitHub",
            e -> {
              // Redirect to GitHub OAuth endpoint or handle GitHub authentication
              getUI().ifPresent(ui -> ui.getPage().setLocation(OAUTH_2_AUTHORIZATION_GITHUB));
            });
    githubButton.getElement().setAttribute("theme", "primary");

    // LDAP Login Form
    if (ldapConfig.isLdapEnabled()) {
      LoginForm ldapLoginForm = new LoginForm();
      ldapLoginForm.setAction("login");
      add(title, githubButton, ldapLoginForm);
    } else {
      add(title, githubButton);
    }
    setAlignItems(Alignment.CENTER);
  }
}
