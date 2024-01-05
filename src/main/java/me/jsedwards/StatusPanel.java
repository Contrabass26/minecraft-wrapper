package me.jsedwards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StatusPanel extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger("StatusPanel");

    private final JProgressBar progressBar; // Should only be accessed on event thread, use setMax and setProgress
    private final JLabel statusLbl; // Should only be accessed on event thread
    private final Timer progressUpdateTimer; // Should only be accessed on event thread
    private final AtomicReference<Supplier<Integer>> progressGetter = new AtomicReference<>(null); // Supplier will be called from event thread

    // Should always be called on event thread
    public StatusPanel() {
        super();
        this.setLayout(new GridBagLayout());
        // Progress bar
        progressBar = new JProgressBar();
        this.add(progressBar, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 20));
        // Status label
        statusLbl = new JLabel("Ready");
        this.add(statusLbl, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 10, 10), 0, 0));
        // Progress bar update thread - actual code is run on event thread
        progressUpdateTimer = new Timer(50, e -> {
            Supplier<Integer> supplier = progressGetter.get();
            if (supplier != null) {
                setProgress(supplier.get());
            }
        });
        progressUpdateTimer.start();
    }

    // Can be called from any thread
    public void saveFileFromUrl(URL in, File out) {
        Thread thread = new Thread(() -> {
            try {
                innerSaveFileFromUrl(in, out);
            } catch (IOException e) {
                LOGGER.error("Failed to download %s to %s".formatted(in.toString(), out.getAbsolutePath()), e);
                setStatus("Download failed");
            }
            progressGetter.set(null);
            setProgress(0);
        });
        thread.start();
    }

    public void saveFileFromUrl(URL in, File out, Runnable onSuccess) {
        Thread thread = new Thread(() -> {
            try {
                innerSaveFileFromUrl(in, out);
                onSuccess.run();
            } catch (IOException e) {
                LOGGER.error("Failed to download %s to %s".formatted(in.toString(), out.getAbsolutePath()), e);
                setStatus("Download failed");
            }
            progressGetter.set(null);
            setProgress(0);
        });
        thread.start();
    }

    public void curlToFile(URL in, File out) {
        new Thread(() -> {
            try {
                FileUtils.copyURLToFile(in, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public Document curlToJsoup(URL in) {
        String command = "curl -s -H \"User-Agent: Mozilla\" \"%s\"".formatted(in.toString());
        try {
            Process process = Runtime.getRuntime().exec(command);
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEachOrdered(content::append);
            }
            process.waitFor();
            return Jsoup.parse(content.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode curlToJson(URL in) {
        String command = "curl -s -H \"User-Agent: Mozilla\" \"%s\"".formatted(in.toString());
        try {
            Process process = Runtime.getRuntime().exec(command);
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEachOrdered(content::append);
            }
            ObjectMapper mapper = new ObjectMapper();
            process.waitFor();
            return mapper.readTree(content.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void innerSaveFileFromUrl(URL in, File out) throws IOException {
        setStatus("Downloading " + in.toString());
        HttpURLConnection connection = (HttpURLConnection) in.openConnection();
        connection.setRequestMethod("GET");
        int contentLength = connection.getContentLength();
        setMax(contentLength);
        InputStream inputStream = connection.getInputStream();
        try (CountingOutputStream outputStream = new CountingOutputStream(new FileOutputStream(out))) {
            progressGetter.set(outputStream::getCount);
            inputStream.transferTo(outputStream);
        }
        inputStream.close();
        LOGGER.info("Downloaded %s to %s".formatted(in.toString(), out.getAbsolutePath()));
        setStatus("Ready");
    }

    public <T> void getJsonFromUrl(URL in, Class<T> jsonClazz, Consumer<T> onSuccess) {
        Thread thread = new Thread(() -> {
            try {
                setStatus("Downloading " + in.toString());
                ObjectMapper mapper = new ObjectMapper();
                T result = mapper.readValue(in, jsonClazz);
                LOGGER.info("Downloaded %s".formatted(in.toString()));
                onSuccess.accept(result);
                setStatus("Ready");
            } catch (IOException e) {
                LOGGER.error("Failed to download %s".formatted(in.toString()), e);
                setStatus("Download failed");
            }
            progressGetter.set(null);
        });
        thread.start();
    }

    public void getJsonNodeFromUrl(URL in, Consumer<JsonNode> onSuccess) {
        Thread thread = new Thread(() -> {
            try {
                setStatus("Downloading " + in.toString());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode result = mapper.readTree(in);
                LOGGER.info("Downloaded %s".formatted(in.toString()));
                onSuccess.accept(result);
                setStatus("Ready");
            } catch (IOException e) {
                LOGGER.error("Failed to download %s".formatted(in.toString()), e);
                setStatus("Download failed");
            }
            progressGetter.set(null);
        });
        thread.start();
    }

    public void getJsoupFromUrl(String in, Consumer<Document> onSuccess) {
        Thread thread = new Thread(() -> {
            try {
                Document document = Jsoup.connect(in).userAgent("Mozilla").get();
                onSuccess.accept(document);
            } catch (IOException e) {
               LOGGER.error("Failed to Jsoup %s".formatted(in));
            }
        });
        thread.start();
    }

    public void exit() {
        progressUpdateTimer.stop();
    }

    private static <T> void setOnEventThread(Consumer<T> setter, T argument) {
        if (SwingUtilities.isEventDispatchThread()) {
            setter.accept(argument);
        } else {
            SwingUtilities.invokeLater(() -> setter.accept(argument));
        }
    }

    public void setMax(int max) {
        setOnEventThread(progressBar::setMaximum, max);
    }

    public void setProgress(int progress) {
        setOnEventThread(progressBar::setValue, progress);
    }
    
    public void setStatus(String status) {
        setOnEventThread(statusLbl::setText, status);
    }
}
