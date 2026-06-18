package com.railwayteam.railways.util;

import java.util.UUID;

public class UsernameUtils {
    public static final UsernameUtils INSTANCE = new UsernameUtils();

    public String getName(UUID uuid) {
        return uuid == null ? "" : uuid.toString();
    }
}
