package com.example.ccperipherals.nfc;

/** The colours NFC cards come in. */
public enum NfcColor {
    RED("red", "Red"),
    GREEN("green", "Green"),
    YELLOW("yellow", "Yellow"),
    PURPLE("purple", "Purple"),
    BLUE("blue", "Blue"),
    WHITE("white", "White");

    private final String id;
    private final String displayName;

    NfcColor(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /** Lower-case name used in item ids and returned to Lua, e.g. "red". */
    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }
}
