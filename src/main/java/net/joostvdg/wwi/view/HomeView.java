/* (C)2024 */
package net.joostvdg.wwi.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.io.Serial;

@PermitAll
@Route(value = "", layout = MainView.class)
public class HomeView extends VerticalLayout {

  @Serial private static final long serialVersionUID = 1L;

  public HomeView() {
    H1 title = new H1("Where Was I?");
    title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

    add(title);
  }
}
