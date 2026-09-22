package org.example.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ScreenshotUtils {
    private ScreenshotUtils() {
    }

    public static void capture(WebDriver driver, String name) {
        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File directory = new File("screenshots");
            Files.createDirectories(directory.toPath());
            Files.copy(source.toPath(), new File(directory, name + ".png").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException exception) {
            System.out.println("Could not save screenshot: " + exception.getMessage());
        }
    }
}
