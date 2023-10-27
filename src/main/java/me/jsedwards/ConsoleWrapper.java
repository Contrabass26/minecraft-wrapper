package me.jsedwards;

import java.io.*;
import java.util.function.Consumer;

public class ConsoleWrapper {

    private final BufferedReader outReader;
    private final BufferedReader errReader;
    private final BufferedWriter writer;

    public ConsoleWrapper(String command, File dir, Consumer<String> output, Consumer<String> error) throws IOException {
        Process process = Runtime.getRuntime().exec(command, null, dir);
        this.outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        new Thread(() -> {
            String line;
            try {
                while ((line = this.outReader.readLine()) != null) {
                    output.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            String line;
            try {
                while ((line = this.errReader.readLine()) != null) {
                    error.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void write(String s) throws IOException {
        writer.write(s);
        writer.flush();
    }
}
