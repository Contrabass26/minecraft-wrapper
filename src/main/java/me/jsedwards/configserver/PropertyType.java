package me.jsedwards.configserver;

import me.jsedwards.Main;

import javax.swing.*;

public enum PropertyType {

    BOOLEAN,
    INTEGER,
    DOUBLE,
    DEFAULT;

    public String inputValue(ConfigProperty property) {
        return (String) JOptionPane.showInputDialog(Main.WINDOW, "Enter new value for %s:".formatted(property.key), "Edit value", JOptionPane.QUESTION_MESSAGE, null, null, property.value);
    }

    private boolean matchesStringType(String type) {
        return true;
    }

    public static PropertyType get(String type) {
        for (PropertyType value : values()) {
            if (value.matchesStringType(type)) {
                return value;
            }
        }
    }
}
