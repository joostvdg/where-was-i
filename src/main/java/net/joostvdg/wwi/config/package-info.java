@ApplicationModule(
    id = "config",
    allowedDependencies = {
      "model_media",
      "model_progress",
      "model_media",
      "model_auth",
      "model_watchlist",
      "media",
      "watchlist"
    }) // because we need to register the Records for reflection
package net.joostvdg.wwi.config;

import org.springframework.modulith.ApplicationModule;
