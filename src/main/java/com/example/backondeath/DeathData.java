package com.example.backondeath;

import org.bukkit.Location;

public class DeathData {
    private final Location deathLocation;
    private final boolean allowed;

    public DeathData(Location deathLocation, boolean allowed) {
        this.deathLocation = deathLocation;
        this.allowed = allowed;
    }

    public Location getDeathLocation() {
        return deathLocation;
    }

    public boolean isAllowed() {
        return allowed;
    }
}
