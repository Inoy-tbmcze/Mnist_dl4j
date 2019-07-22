package com.myNN.blahblahDraw;


import com.myNN.ImagePipe;
import com.myNN.MyNNMnist;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class blblDraw {

    JButton clearBtn, whiteBtn, goBtn, greenBtn, redBtn, magentaBtn;
    DrawArea drawArea;
    ActionListener actionListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == clearBtn) {
                drawArea.clear();
            } else if (e.getSource() == whiteBtn) {
                drawArea.white();
            } else if (e.getSource() == goBtn) {
                //TODO send to pipe
                try {
                    Image tmpImage = drawArea.getImage();
                    SaveImageAsFile(tmpImage, "saved.png");
                    Image newImage = tmpImage.getScaledInstance(ImagePipe.getWidth(), ImagePipe.getHeight(), Image.SCALE_AREA_AVERAGING);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(new File("saved.png"))));
                    SaveImageAsFile(newImage, "rescaled.png");
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(new File("rescaled.png"))));
                    Image newTrimmedScaledImage = TrimBlack.trim("saved.png", "trimmed.png", 100).getScaledInstance(ImagePipe.getWidth(), ImagePipe.getHeight(), Image.SCALE_AREA_AVERAGING);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(new File("trimmed.png"))));
                    SaveImageAsFile(newTrimmedScaledImage, "trimmedRescaled.png");
                    MyNNMnist.processOneImage(ImagePipe.processFile("trimmedRescaled.png"));
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(new File("trimmedRescaled.png"))));
                } catch (IOException ex) {
                    System.out.println(ex);
                }

            }
        }
    };

    public static void gogoDraw() {
        new blblDraw().show();

    }

    public void show() {
        // create main frame
        JFrame frame = new JFrame("Swing Paint");
        Container content = frame.getContentPane();
        // set layout on content pane
        content.setLayout(new BorderLayout());
        // create draw area
        drawArea = new DrawArea();

        // add to content pane
        content.add(drawArea, BorderLayout.CENTER);

        // create controls to apply colors and call clear feature
        JPanel controls = new JPanel();

        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(actionListener);
        whiteBtn = new JButton("White");
        whiteBtn.addActionListener(actionListener);
        goBtn = new JButton("b2");
        goBtn.addActionListener(actionListener);
        greenBtn = new JButton("g3");
//        greenBtn.addActionListener(actionListener);
        redBtn = new JButton("r4");
//        redBtn.addActionListener(actionListener);
        magentaBtn = new JButton("m5");
//        magentaBtn.addActionListener(actionListener);

        // add to panel
//        controls.add(greenBtn);
        controls.add(goBtn);
        controls.add(whiteBtn);
//        controls.add(redBtn);
//        controls.add(magentaBtn);
        controls.add(clearBtn);

        // add to content pane
        content.add(controls, BorderLayout.NORTH);

        frame.setSize(280, 345);
        // can close frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // show the swing paint result
        frame.setVisible(true);
    }

    private void SaveImageAsFile(Image img, String FileName) throws IOException {
        BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D bGr = image.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        File outputFile = new File(FileName);
        ImageIO.write(image, "png", outputFile);


    }
}
