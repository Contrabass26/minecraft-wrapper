package me.jsedwards.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class UnifiedListenerTextField extends JTextField implements DocumentListener {

    public UnifiedListenerTextField() {
        super();
        this.getDocument().addDocumentListener(this);
    }

    @Override
    public String getText() {
        return super.getText().strip();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update();
    }

    protected abstract void update();
}
