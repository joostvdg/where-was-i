package net.joostvdg.wwi.main;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;

import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import net.joostvdg.wwi.tracking.WatchListView;
import net.joostvdg.wwi.user.UserProfileView;

public class MainView extends AppLayout {

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Where Was I?");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        SideNav nav = new SideNav();
        SideNavItem homeLink = new SideNavItem("Home",
                HomeView.class, VaadinIcon.HOME.create());
        SideNavItem profileLink = new SideNavItem("Profile",
                UserProfileView.class, VaadinIcon.USER_CARD.create());
        SideNavItem watchListLink = new SideNavItem("Watch List",
                WatchListView.class, VaadinIcon.LIST.create());
        SideNavItem vaadinLink = new SideNavItem("Vaadin website",
                "https://vaadin.com", VaadinIcon.VAADIN_H.create());

        nav.addItem(homeLink, profileLink, watchListLink, vaadinLink);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(toggle, title);
    }



}
