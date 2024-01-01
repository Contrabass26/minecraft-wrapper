package me.jsedwards;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StatusPanel extends JPanel {

    private static final Logger LOGGER = LogManager.getLogger();

    private final JProgressBar progressBar; // Should only be accessed on event thread, use setMax and setProgress
    private final JLabel statusLbl; // Should only be accessed on event thread
    private final Timer progressUpdateTimer; // Should only be accessed on event thread
    private final AtomicReference<Supplier<Integer>> progressGetter = new AtomicReference<>(() -> 0); // Supplier will be called from event thread

    // Should always be called on event thread
    public StatusPanel() {
        super();
        this.setLayout(new GridBagLayout());
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setValue(75);
        this.add(progressBar, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 20));
        // Status label
        statusLbl = new JLabel("Ready");
        this.add(statusLbl, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 10, 10), 0, 0));
        // Progress bar update thread - actual code is run on event thread
        progressUpdateTimer = new Timer(50, e -> setProgress(progressGetter.get().get()));
        progressUpdateTimer.start();
    }

    // Can be called from any thread
    public void downloadFile(URL in, File out) {
        Thread downloadThread = new Thread(() -> {
            try {
                setStatus("Downloading " + in.getFile());
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
                LOGGER.info("Downloaded %s to %s".formatted(in.getFile(), out.getAbsolutePath()));
                setStatus("Ready");
            } catch (IOException e) {
                LOGGER.error("Failed to download %s to %s".formatted(in.getFile(), out.getAbsolutePath()), e);
                setStatus("Download failed");
            }
            progressGetter.set(() -> 0);
        });
        downloadThread.start();
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
