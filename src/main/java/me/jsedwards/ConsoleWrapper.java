package me.jsedwards;

import java.io.*;
import java.util.function.Consumer;

public class ConsoleWrapper {

    private final BufferedReader outReader;
    private final BufferedReader errReader;
    private final BufferedWriter writer;
    private final Process process;

    public ConsoleWrapper(String command, File dir, Consumer<String> output, Consumer<String> error) throws IOException {
        this.process = Runtime.getRuntime().exec(command, null, dir);
        this.outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        new Thread(() -> {
            String line;
            try {
                while ((line = this.outReader.readLine()) != null) {
                    output.accept(line);
                }
                System.out.println("Output finished");
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
                System.out.println("Error finished");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void write(String s) throws IOException {
        writer.write(s);
        writer.flush();
    }

    public static void main(String[] args) {
        try {
            ConsoleWrapper console = new ConsoleWrapper("java -Xmx2G -jar fabric-server-launch.jar nogui", new File("/Users/josephedwards/Downloads/test-server"), System.out::println, System.err::println);
//            console.write("a = 52\n");
//            console.write("print(a ** 4)\n");
            console.process.getOutputStream().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
