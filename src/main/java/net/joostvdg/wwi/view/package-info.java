@ApplicationModule(
    type = ApplicationModule.Type.CLOSED,
    allowedDependencies = {"user", "shared", "config", "media", "progress", "watchlist"})
package net.joostvdg.wwi.view;

import org.springframework.modulith.ApplicationModule;
