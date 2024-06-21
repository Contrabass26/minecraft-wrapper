package me.jsedwards.configserver;

import me.jsedwards.Main;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

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

        @Override
        protected boolean isApplicable(Object example) {
            return example instanceof Boolean;
        }

        @Override
        public Object convert(String s) {
            return Boolean.parseBoolean(s);
        }
    },
    INTEGER {
        @Override
        protected boolean isApplicable(String type, String defaultValue) {
            return type != null && type.toLowerCase().contains("int") || StringUtils.isNumeric(defaultValue);
        }

        @Override
        protected boolean isApplicable(Object example) {
            return example instanceof Integer;
        }

        @Override
        public Object convert(String s) {
            return Integer.parseInt(s);
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

        @Override
        protected boolean isApplicable(Object example) {
            return example instanceof Float || example instanceof Double;
        }

        @Override
        public Object convert(String s) {
            return Double.parseDouble(s);
        }
    },
    LIST {
        // String format: [item1, item2, item3]

        @Override
        protected boolean isApplicable(String type, String defaultValue) {
            return false;
        }

        @Override
        protected boolean isApplicable(Object example) {
            return example instanceof List;
        }

        @Override
        public Object convert(String s) {
            return Arrays.stream(s.substring(1, s.length() - 1).split(", ")).toList();
        }
    },
    STRING;

    public String inputValue(ConfigProperty property) {
        String input = (String) JOptionPane.showInputDialog(Main.WINDOW, "Enter new value for %s:".formatted(property.key), "Edit value", JOptionPane.QUESTION_MESSAGE, null, null, property.value);
        if (input == null) return property.value;
        return input;
    }

    protected boolean isApplicable(String type, String defaultValue) {
        return true;
    }

    protected boolean isApplicable(Object example) {
        return true;
    }

    public Object convert(String s) {
        return String.valueOf(s);
    }

    public static PropertyType get(String type, String defaultValue) {
        for (PropertyType value : values()) {
            if (value.isApplicable(type, defaultValue)) {
                return value;
            }
        }
        throw new IllegalStateException("No property type found");
    }

    public static PropertyType get(Object example) {
        for (PropertyType type : values()) {
            if (type.isApplicable(example)) {
                return type;
            }
        }
        throw new IllegalStateException("No property type found");
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase());
    }
}
