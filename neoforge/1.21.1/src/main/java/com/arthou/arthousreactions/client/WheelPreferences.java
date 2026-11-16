package com.arthou.arthousreactions.client;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Local wheel preferences, independent of a world or server. */
final class WheelPreferences {
    private final Path file;
    private boolean previewEnabled;
    private int page;

    WheelPreferences(Path file) {
        this.file = file;
        if (!Files.exists(file)) return;
        var values = new Properties();
        try (var input = Files.newInputStream(file)) {
            values.load(input);
            previewEnabled = Boolean.parseBoolean(values.getProperty("preview", "false"));
            page = Math.max(0, Integer.parseInt(values.getProperty("page", "0")));
        } catch (IOException | IllegalArgumentException exception) {
            LogUtils.getLogger().warn("Could not read reaction wheel preferences: {}", file, exception);
        }
    }

    boolean previewEnabled() { return previewEnabled; }
    int page(int pageCount) { return Math.clamp(page, 0, Math.max(0, pageCount - 1)); }

    void togglePreview() {
        previewEnabled = !previewEnabled;
        save();
    }

    void setPage(int value) {
        if (page == Math.max(0, value)) return;
        page = Math.max(0, value);
        save();
    }

    private void save() {
        var values = new Properties();
        values.setProperty("preview", Boolean.toString(previewEnabled));
        values.setProperty("page", Integer.toString(page));
        Path temporary = null;
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "reactions-wheel-", ".tmp");
            try (var output = Files.newOutputStream(temporary)) { values.store(output, "Arthou's Reactions wheel"); }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LogUtils.getLogger().warn("Could not save reaction wheel preferences: {}", file, exception);
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
        }
    }
}
