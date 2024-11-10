package net.joostvdg.wwi.main;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {
    // URL that Spring Security uses to connect to Google services
    private static final String OAUTH_2_AUTHORIZATION_GITHUB = "/oauth2/authorization/github";
    private final Logger logger = LoggerFactory.getLogger(LoginView.class);

    public LoginView() {
        // Title for the login page
        H1 title = new H1("Login Page");

        // GitHub Login Button
        Button githubButton = new Button("Login with GitHub", e -> {
            // Redirect to GitHub OAuth endpoint or handle GitHub authentication
            getUI().ifPresent(ui -> ui.getPage().setLocation(OAUTH_2_AUTHORIZATION_GITHUB));
        });
        githubButton.getElement().setAttribute("theme", "primary");

        // LDAP Login Form
        LoginForm ldapLoginForm = new LoginForm();
//        ldapLoginForm.addLoginListener(event -> {
//            String username = event.getUsername();
//            String password = event.getPassword();
//            // Handle LDAP authentication logic here
//            if (authenticateWithLdap(username, password)) {
//                // Navigate to the main application or success page
//                ViewNotifications.showSuccessNotification("Login successful");
//                getUI().ifPresent(ui -> ui.navigate("main"));
//            } else {
//                logger.warn("Authentication failed for user: " + username);
//                ViewNotifications.showErrorNotification("Login failed invalid username or password");
//                ldapLoginForm.setError(true);  // Display error if authentication fails
//            }
//        });
        ldapLoginForm.setAction("login");

        // Add components to the layout
        add(title, githubButton, ldapLoginForm);
        setAlignItems(Alignment.CENTER);
    }
//
//    private boolean authenticateWithLdap(String username, String password) {
//        // Implement LDAP authentication logic (this is just a placeholder)
//        // This could involve connecting to an LDAP server and validating the user credentials
//        logger.info("Authenticating user: " + username);
//        return "user".equals(username) && "password".equals(password);  // Replace with real LDAP check
//    }
}