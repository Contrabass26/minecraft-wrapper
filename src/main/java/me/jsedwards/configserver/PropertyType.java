package me.jsedwards.configserver;

import me.jsedwards.Main;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public enum PropertyType {

    BOOLEAN {
        @Override
        protected boolean isApplicable(String type, String defaultValue) {
            return type != null && type.toLowerCase().contains("bool") || defaultValue != null && (defaultValue.equalsIgnoreCase("true") || defaultValue.equalsIgnoreCase("false"));
        }
    },
    INTEGER {
        @Override
        protected boolean isApplicable(String type, String defaultValue) {
            return type != null && type.toLowerCase().contains("int") || StringUtils.isNumeric(defaultValue);
        }
    },
    DOUBLE {
        @Override
        protected boolean isApplicable(String type, String defaultValue) {
            if (type != null) {
                String lowerType = type.toLowerCase();
                if (lowerType.contains("float") || lowerType.contains("double") || lowerType.contains("real")) return true;
            }
            return defaultValue != null && defaultValue.matches("[0-9.]+");
        }
    },
    DEFAULT;

    public String inputValue(ConfigProperty property) {
        return (String) JOptionPane.showInputDialog(Main.WINDOW, "Enter new value for %s:".formatted(property.key), "Edit value", JOptionPane.QUESTION_MESSAGE, null, null, property.value);
    }

    protected boolean isApplicable(String type, String defaultValue) {
        return true;
    }

    public static PropertyType get(String type, String defaultValue) {
        for (PropertyType value : values()) {
            if (value.isApplicable(type, defaultValue)) {
                return value;
            }
        }
        throw new IllegalStateException("No property type found");
    }
}
