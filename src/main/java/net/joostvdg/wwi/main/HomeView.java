package net.joostvdg.wwi.main;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainView.class)
public class HomeView  extends VerticalLayout {

    public HomeView() {
        H1 title = new H1("Where Was I?");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        add(title);
    }
}
