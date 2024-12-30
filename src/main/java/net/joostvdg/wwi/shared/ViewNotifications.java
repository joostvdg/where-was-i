package net.joostvdg.wwi.shared;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ViewNotifications {

    private ViewNotifications() {
        // Utility class
    }

    public static void showSuccessNotification(String message) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        Icon notificationIcon = VaadinIcon.CHECK.create();
        notificationIcon.setColor("green");

        Span messageSpan = new Span(message);

        HorizontalLayout layout = new HorizontalLayout(notificationIcon, messageSpan);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.setDuration(3000); // Duration in milliseconds
        notification.open();
    }

    public static void showErrorNotification(String message) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon notificationIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
        notificationIcon.setColor("red");

        Span messageSpan = new Span(message);

        HorizontalLayout layout = new HorizontalLayout(notificationIcon, messageSpan);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.setDuration(3000); // Duration in milliseconds
        notification.open();
    }
}
