package com.raddatz.automation.data;

import java.util.HashMap;
import java.util.Map;

/** Current-JVM identities proved through owned UI data, never a cross-run record cache. */
public final class OwnedRecordIds {
    private static final Map<String, String> COMPANIES = new HashMap<>();
    private static final Map<String, String> PROPERTIES = new HashMap<>();
    private OwnedRecordIds() { }

    public static void company(String email, String id) { register(COMPANIES, email, id); }
    public static void property(String license, String id) { register(PROPERTIES, license, id); }
    public static String company(String email) { return require(COMPANIES, email); }
    public static String property(String license) { return require(PROPERTIES, license); }
    public static void clear() { COMPANIES.clear(); PROPERTIES.clear(); }

    private static void register(Map<String, String> ids, String key, String id) {
        if (id == null || !id.matches("[1-9][0-9]*")) throw new IllegalStateException("Invalid owned record identity");
        String previous = ids.putIfAbsent(key, id);
        if (previous != null && !previous.equals(id)) throw new IllegalStateException("Owned fixture identity changed unexpectedly");
    }

    private static String require(Map<String, String> ids, String key) {
        String id = ids.get(key);
        if (id == null) throw new IllegalStateException("Required parent was not created/ownership-verified in this JVM run");
        return id;
    }
}
