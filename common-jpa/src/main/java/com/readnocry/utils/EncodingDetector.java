package com.readnocry.utils;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncodingDetector {
    public static String detectEncoding(Path filePath) throws IOException {
        try (InputStream stream = Files.newInputStream(filePath)) {
            byte[] fileContent = stream.readAllBytes();
            CharsetDetector detector = new CharsetDetector();
            detector.setText(fileContent);
            CharsetMatch match = detector.detect();
            return match.getName();
        }
    }
}