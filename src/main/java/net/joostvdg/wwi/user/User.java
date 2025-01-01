/* (C)2024 */
package net.joostvdg.wwi.user;

import java.time.LocalDate;

public record User(
    int id,
    String accountNumber,
    String accountType,
    String username,
    String name,
    String email,
    LocalDate dateJoined,
    LocalDate dateLastLogin) {}
