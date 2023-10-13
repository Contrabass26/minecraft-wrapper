package com.contrabass;

import com.formdev.flatlaf.FlatDarculaLaf;

public class Main {

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true"); // Use default menu bar on MacOS
        System.setProperty("apple.awt.application.name", "Minecraft Wrapper"); // Must happen before any AWT classes are loaded
        FlatDarculaLaf.setup();
    }
}
