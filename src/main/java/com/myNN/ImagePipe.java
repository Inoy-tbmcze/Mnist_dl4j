package com.myNN;


import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ImagePipe {
    //    private static Logger log = LoggerFactory.getLogger(ImagePipe.class);
    private static int height = 28;
    private static int width = 28;
    private static int channels = 1;
    private static int seed = 111;
    private static Random randNumG = new Random(seed);
    private static int batchSize = 32;
    private static int output = 10;
    private static File trainData;
    private static String trainDataZip = "./training.zip";
    private static File testData;
    private static String testDataZip = "./testing.zip";
    private static ParentPathLabelGenerator labelMaker;
    private static java.util.List<String> labelList;


    public static int getChannels() {
        return channels;
    }

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static int getSeed() {
        return seed;
    }

    public static int getBatchSize() {
        return batchSize;
    }

    public static int getOutput() {
        return output;
    }

    public static File getTrainData() {
        return trainData;
    }

    public static File getTestData() {
        return testData;
    }

    public static List<String> getLables() {
        try {
            if (labelList == null) {
                getTrainIterator();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return labelList;
    }

    static DataSetIterator getTrainIterator() throws IOException {
        if (trainData == null) {
            FileUnzipper unzipper = new FileUnzipper(trainDataZip, "./");
            unzipper.unzip();
            trainData = new File("./training");
        }
        // FileSplit(path, formats, random)
        FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumG);

        //lables
        labelMaker = new ParentPathLabelGenerator();

        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);

        //initialize rr

        recordReader.initialize(train);

        labelList = recordReader.getLabels();


        //Iterator
        DataSetIterator dataI = new RecordReaderDataSetIterator(recordReader, batchSize, 1, output);

        //Scale pixel brightness to 0 - 1

        DataNormalization scaler = new ImagePreProcessingScaler(0, 0.9999);
        scaler.fit(dataI);
        dataI.setPreProcessor(scaler);


        return dataI;

    }

    static DataSetIterator getTestIterator() throws IOException {

        if (testData == null) {
            FileUnzipper unzipper = new FileUnzipper(testDataZip, "./");
            unzipper.unzip();
            testData = new File("./testing");
        }
        // FileSplit(path, formats, random)
        FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumG);

        //lables
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);

        //initialize rr
        recordReader.initialize(test);

        //Iterator
        DataSetIterator dataI = new RecordReaderDataSetIterator(recordReader, batchSize, 1, output);

        //Scale pixel brightness to 0 - 1
        DataNormalization scaler = new ImagePreProcessingScaler(0, 0.9999);
        scaler.fit(dataI);
        dataI.setPreProcessor(scaler);


        return dataI;

    }

    static public INDArray processImage(Image img) throws IOException {

        NativeImageLoader loader = new NativeImageLoader(height, width, channels);
        INDArray image = loader.asMatrix(img);

        //rescale
        DataNormalization scaler = new ImagePreProcessingScaler(0, 0.9999);
        scaler.transform(image);

        System.out.println(image);
        return image;

    }

    static public String ChooseFile() {
        JFileChooser fc = new JFileChooser();
        int ret = fc.showOpenDialog(null);
        File file;
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            String filename = file.getAbsolutePath();
            return filename;
        } else {
            return null;
        }
    }

    static public INDArray processFile(String path) throws IOException {
        File file;
        if (path != null) {
            file = new File(path);
        } else {

            file = new File(ChooseFile());
        }
        NativeImageLoader loader;
        if (path.equals("trimmedRescaled.png")) {
            loader = new NativeImageLoader();
        } else {
            loader = new NativeImageLoader(height, width, channels);
        }

        INDArray image = loader.asMatrix(file);

        //rescale
        DataNormalization scaler = new ImagePreProcessingScaler(0, 0.9999);
        scaler.transform(image);
//        System.out.println(image);
        System.out.println(path);
        return image;

    }


}
