package me.jsedwards.dashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ConsoleWrapper {

    private static final Logger LOGGER = LogManager.getLogger();

    private final BufferedReader outReader;
    private final BufferedReader errReader;
    private final BufferedWriter writer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Process process;

    public ConsoleWrapper(String command, File dir, Consumer<String> output, Consumer<String> error, Runnable onFinish) throws IOException { // onFinish is not run on the calling thread
        process = Runtime.getRuntime().exec(command, null, dir);
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
                LOGGER.error("Failed to write output from server", e);
            }
        }).start();
        new Thread(() -> {
            String line;
            try {
                while ((line = this.errReader.readLine()) != null) {
                    error.accept(line);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to write error from server", e);
            }
        }).start();
        new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                LOGGER.warn("Process was interrupted", e);
            }
            running.set(false);
            onFinish.run();
        }).start();
    }

    public void write(String s) throws IOException {
        writer.write(s);
        writer.flush();
    }

    public boolean isRunning() {
        return running.get();
    }

    public void forceStop() {
        process.destroyForcibly();
    }
}
