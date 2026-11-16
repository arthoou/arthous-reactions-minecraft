package com.arthou.arthousreactions.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class WheelPreferencesTest {
    @TempDir Path directory;

    @Test void previewStartsDisabledAndChangesSurviveNewGuiAndGameSessions() {
        Path file = directory.resolve("config/wheel.properties");
        var initial = new WheelPreferences(file);
        assertFalse(initial.previewEnabled());
        assertEquals(0, initial.page(2));
        initial.togglePreview();
        initial.setPage(1);
        var reopened = new WheelPreferences(file);
        assertTrue(reopened.previewEnabled());
        assertEquals(1, reopened.page(2));
        reopened.togglePreview();
        reopened.setPage(0);
        var restarted = new WheelPreferences(file);
        assertFalse(restarted.previewEnabled());
        assertEquals(0, restarted.page(2));
    }

    @Test void savedPageOpensDirectlyWithoutFlashingTheFirstPage() {
        var settings = new WheelPreferences(directory.resolve("wheel.properties"));
        settings.setPage(1);
        var restored = new WheelPreferences(directory.resolve("wheel.properties"));
        var motion = new WheelMotion(15, 12, 1000, restored.page(2));
        assertEquals(1, motion.page());
        assertEquals(0, motion.pageOpacity(0, 1000));
        assertEquals(1, motion.pageOpacity(1, 1000));
        motion.navigate(1, 1010);
        settings.setPage(motion.page());
        assertEquals(0, new WheelPreferences(directory.resolve("wheel.properties")).page(2));
    }

    @Test void invalidOrOutOfRangePagesCannotBreakTheWheel() throws Exception {
        Path file = directory.resolve("wheel.properties");
        Files.writeString(file, "preview=true\npage=999\n");
        var settings = new WheelPreferences(file);
        assertTrue(settings.previewEnabled());
        assertEquals(1, settings.page(2));
        assertEquals(0, settings.page(1));
        Files.writeString(file, "page=invalid\n");
        assertEquals(0, new WheelPreferences(file).page(2));
    }
}
