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

        @Override
        public String inputValue(ConfigProperty property) {
            JCheckBox checkBox = new JCheckBox(property.key);
            checkBox.setSelected(property.value.equalsIgnoreCase("true"));
            int response = JOptionPane.showConfirmDialog(Main.WINDOW, checkBox, "Edit value", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
            if (response == JOptionPane.OK_OPTION) {
                return String.valueOf(checkBox.isSelected());
            }
            return property.value;
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
    STRING;

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

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase());
    }
}
