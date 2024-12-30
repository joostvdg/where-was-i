package net.joostvdg.wwi.view;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.joostvdg.wwi.user.UserService;

@Route(value = "profile", layout = MainView.class)
@PageTitle("Profile | Where Was I?")
@PermitAll
public class UserProfileView extends VerticalLayout {

    private final UserService userService;

    public UserProfileView(UserService userService) {
        this.userService = userService;
        add(createReadOnlyUserProfile());
    }

    // A method to create the read-only user profile view
    private FormLayout createReadOnlyUserProfile() {
        // Read-only data (this would usually come from a backend service)
        var user = userService.getLoggedInUser();

        // Create Span components to display the data
        Span accountNumberSpan = new Span(user.accountNumber());
        Span accountTypeSpan = new Span(user.accountType());
        Span usernameSpan = new Span(user.username());
        Span emailSpan = new Span(user.email());
        Span dateJoinedSpan = new Span(user.dateJoined().toString());
        Span dateLastLoginSpan = new Span(user.dateLastLogin().toString());

        // Create a FormLayout and add the Spans as read-only fields
        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(accountNumberSpan, "Account Number");
        formLayout.addFormItem(accountTypeSpan, "Account Type");
        formLayout.addFormItem(usernameSpan, "Username");
        formLayout.addFormItem(emailSpan, "Email");
        formLayout.addFormItem(dateJoinedSpan, "Date Joined");
        formLayout.addFormItem(dateLastLoginSpan, "Date Last Login");

        // Optionally, you can adjust layout settings (like column spans)
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),  // One column layout on smaller screens
                new FormLayout.ResponsiveStep("500px", 2) // Two columns layout on wider screens
        );

        return formLayout;
    }
}