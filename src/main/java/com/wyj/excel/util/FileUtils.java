package com.wyj.excel.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUtils {

    public static final String tmpDir = System.getProperty("java.io.tmpdir");

    public static void createParentFile(File file) {
        Assert.notNull(file, "file can't be NULL");
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    public static void copyFile(final File srcFile, final File destFile) {
        try {
            org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createTmpFile(String suffix) {
        String fileName = tmpDir + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
        return new File(fileName);
    }

    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            throw new RuntimeException("This file is the directory!");
        }
        file.delete();
    }

}
