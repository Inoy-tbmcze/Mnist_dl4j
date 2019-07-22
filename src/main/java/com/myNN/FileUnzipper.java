package com.myNN;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUnzipper {


    private String zipFileName;
    private String unzipDir;

    public FileUnzipper(String zipFileName, String unzipDir) {
        this.zipFileName = zipFileName;
        this.unzipDir = unzipDir;
    }

    public void unzip() {
        String zipFilePath = this.zipFileName;
        try {
            System.out.println("zipFilePath = " + zipFilePath);
            ZipFile zipFile = new ZipFile(zipFilePath);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    System.out.print("dir  : " + entry.getName());
                    String destPath = this.unzipDir + File.separator + entry.getName();
                    System.out.println(" => " + destPath);
                    File file = new File(destPath);
                    file.mkdirs();
                } else {
                    String destPath = this.unzipDir + File.separator + entry.getName();

                    try (InputStream inputStream = zipFile.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(destPath);
                    ) {
                        int data = inputStream.read();
                        while (data != -1) {
                            outputStream.write(data);
                            data = inputStream.read();
                        }
                    }
//                    System.out.println("file : " + entry.getName() + " => " + destPath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error unzipping file " + zipFilePath, e);
        }
    }
}