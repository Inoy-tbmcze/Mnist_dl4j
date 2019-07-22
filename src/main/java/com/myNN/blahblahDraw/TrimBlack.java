package com.myNN.blahblahDraw;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TrimBlack {
    private static BufferedImage img;

    public TrimBlack(File input) {
        try {
            img = ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException("Problem reading image", e);
        }
    }

    public static Image trim(String filePath, String outFilePath, int px) {
        TrimBlack trim = new TrimBlack(new File(filePath));
        trim.trim(px);
        trim.write(new File(outFilePath));
        return img;
    }

    public void trim(int px) {
        BufferedImage newImg = new BufferedImage(img.getHeight() + 200, img.getWidth() + 200,
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = newImg.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, newImg.getHeight(), newImg.getWidth());
        g.drawImage(img, 100, 100, null);
        img = newImg;

        int startW = getStartOfTrimWidth(img);
        int startH = getStartOfTrimHeight(img);

        startH -= px / 2;

        startW -= px / 2;

        BufferedImage newImg1 = new BufferedImage(img.getHeight() - startH, img.getWidth() - startW,
                BufferedImage.TYPE_BYTE_GRAY);
        g = newImg1.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, newImg1.getHeight(), newImg1.getWidth());
        g.drawImage(img, -startW, -startH, null);
        img = newImg1;

        int endW = getTrimmedWidth(img);
        int endH = getTrimmedHeight(img);

        endH += px / 2;

        endW += px / 2;

        BufferedImage newImg2 = new BufferedImage(endW, endH,
                BufferedImage.TYPE_BYTE_GRAY);
        g = newImg2.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, newImg1.getHeight(), newImg1.getWidth());
        g.drawImage(img, 0, 0, null);
        img = newImg2;

        int size = img.getHeight() > img.getWidth() ? img.getHeight() : img.getWidth();

        BufferedImage newImg3 = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        g = newImg3.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, size, size);
        if (img.getWidth() > img.getHeight()) {
            g.drawImage(img, 0, (img.getWidth() - img.getHeight()) / 2, null);
        } else if (img.getHeight() > img.getWidth()) {
            g.drawImage(img, (img.getHeight() - img.getWidth()) / 2, 0, null);
        } else {
            g.drawImage(img, 0, 0, null);
        }
        img = newImg3;
    }

    public void write(File f) {
        try {
            ImageIO.write(img, "png", f);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing image", e);
        }
    }

    private int getTrimmedWidth(BufferedImage img) {
        int height = this.img.getHeight();
        int width = this.img.getWidth();
        int trimmedWidth = 0;

        for (int i = 0; i < height; i++) {
            for (int j = width - 1; j >= 0; j--) {
                if (img.getRGB(j, i) != Color.BLACK.getRGB() &&
                        j > trimmedWidth) {
                    trimmedWidth = j;
                    break;
                }
            }
        }

        return trimmedWidth;
    }

    private int getTrimmedHeight(BufferedImage img) {
        int width = this.img.getWidth();
        int height = this.img.getHeight();
        int trimmedHeight = 0;

        for (int i = 0; i < width; i++) {
            for (int j = height - 1; j >= 0; j--) {
                if (img.getRGB(i, j) != Color.BLACK.getRGB() &&
                        j > trimmedHeight) {
                    trimmedHeight = j;
                    break;
                }
            }
        }

        return trimmedHeight;
    }

    private int getStartOfTrimWidth(BufferedImage img) {
        int height = this.img.getHeight();
        int width = this.img.getWidth();
        int StartOfTrimWidth = this.img.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (img.getRGB(j, i) != Color.BLACK.getRGB() &&
                        j < StartOfTrimWidth) {
                    StartOfTrimWidth = j;
                    break;
                }
            }
        }
        return StartOfTrimWidth;
    }

    private int getStartOfTrimHeight(BufferedImage img) {
        int height = this.img.getHeight();
        int width = this.img.getWidth();
        int StartOfTrimHeight = this.img.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (img.getRGB(i, j) != Color.BLACK.getRGB() &&
                        j < StartOfTrimHeight) {
                    StartOfTrimHeight = j;
                    break;
                }
            }
        }
        return StartOfTrimHeight;
    }
}