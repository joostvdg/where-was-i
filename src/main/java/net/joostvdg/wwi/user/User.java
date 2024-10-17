package net.joostvdg.wwi.user;

import net.joostvdg.wwi.media.Progress;
import java.time.LocalDate;
import java.util.Set;

public record User(
        long id,
        String accountNumber,
        String accountType,
        String username,
        String name,
        String email,
        LocalDate dateJoined,
        LocalDate dateLastLogin,
        Set<Progress> progress
) {

}