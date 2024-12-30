package net.joostvdg.wwi;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ApplicationTest {

    @Test
    void createApplicationModuleModel() {
        ApplicationModules modules = ApplicationModules.of(Application.class);
        modules.forEach(System.out::println);
        modules.verify();
    }
}
