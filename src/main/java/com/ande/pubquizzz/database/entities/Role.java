package com.ande.pubquizzz.database.entities;

public enum Role {
    ADMIN,
    USER;

    public String springSecurityAuthority() {
        return "ROLE_" + this.name();
    }
}