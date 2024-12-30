package net.joostvdg.wwi.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;

import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@PermitAll
public class MainView extends AppLayout {

    private static final String LOGOUT_SUCCESS_URL = "/";

    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();

        String username = "Unknown";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verify how the user is authenticated, could be OAuth2, LDAP, or OIDC (e.g. Keycloak)
        logger.info("Authentication: {}", authentication.getClass().getName());
        logger.info("Principal: {}" , authentication.getPrincipal().getClass().getName());

        switch (authentication.getClass().getName()) {
            case "org.springframework.security.authentication.UsernamePasswordAuthenticationToken":
                logger.info("Principal is UsernamePasswordAuthenticationToken");
                username = authentication.getName();
                break;
            case "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken":
                logger.info("Principal is OAuth2AuthenticationToken");
                OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
                username = principal.getAttribute("name");
                // TODO: update user login time
                principal.getAuthorities().forEach(authority -> {
                    logger.info("Authority: {}", authority.getAuthority());
                });
                break;
            default:
                logger.warn("Principal is unknown");
        }


        H1 title = new H1("Where Was I? - " + username);
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");


        SideNav nav = new SideNav();
        SideNavItem homeLink = new SideNavItem("Home",
                HomeView.class, VaadinIcon.HOME.create());
        SideNavItem profileLink = new SideNavItem("Profile",
                UserProfileView.class, VaadinIcon.USER_CARD.create());
        SideNavItem watchListLink = new SideNavItem("Watch List",
                WatchListView.class, VaadinIcon.LIST.create());

        SideNavItem movieLink = new SideNavItem("Movies",
                MovieListView.class, VaadinIcon.MOVIE.create());
        SideNavItem videoGameLink = new SideNavItem("Games",
                VideoGameListView.class, VaadinIcon.GAMEPAD.create());
        SideNavItem seriesLink = new SideNavItem("Series",
                SeriesListView.class, VaadinIcon.PICTURE.create());

        SideNavItem vaadinLink = new SideNavItem("Vaadin website",
                "https://vaadin.com", VaadinIcon.VAADIN_H.create());

        nav.addItem(homeLink, profileLink, watchListLink,movieLink, videoGameLink, seriesLink,vaadinLink);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        Button logoutButton = new Button("Logout", click -> {
            UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                    null);
        });
        logoutButton.setIcon(VaadinIcon.SIGN_OUT.create());

        addToDrawer(scroller);
        addToNavbar(toggle, title, logoutButton);
    }



}
