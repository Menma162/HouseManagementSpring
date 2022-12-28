package com.HouseManagement.HouseManagement.other;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConvertImage {
    public static String toBase64(String path) throws IOException {
        File file = new File(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        Base64 base64 = new Base64();
        byte[] encodeBase64 = base64.encode(fileContent);
        return new String(encodeBase64, StandardCharsets.UTF_8);
    }
}
