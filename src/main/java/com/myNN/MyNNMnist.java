package com.myNN;


import com.myNN.blahblahDraw.blblDraw;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MyNNMnist {

    static int height;
    static int width;
    static int seed;
    static int batchSize;
    static int output;
    static int channels;
    static IUpdater updater;
    static int MaxEpochs;
    static int l1 = 20;
    static int l2 = 20;

    static MultiLayerNetwork network;

    static DataSetIterator train;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        height = ImagePipe.getHeight();
        width = ImagePipe.getWidth();
        seed = ImagePipe.getSeed();
        batchSize = ImagePipe.getBatchSize();
        output = ImagePipe.getOutput();
        channels = ImagePipe.getChannels();
        MaxEpochs = 10;
        updater = new Nesterovs(0.006, 0.9);
//        DataSetIterator trainIterator = ImagePipe.getTrainIterator(); // get labels beforehand

        boolean f = true;
        while (f) {
            switch (reader.readLine()) {
                case "exit":
                    f = false;
                    break;
                case "build": {
                    try {
                        System.out.println("Enter l1");
                        l1 = Integer.parseInt(reader.readLine());
                        System.out.println("Enter l2 or 0");
                        l2 = Integer.parseInt(reader.readLine());
                        System.out.println("Enter updater type(n for Nesterov's ; a for Adam)");
                        switch (reader.readLine()) {
                            case "n":
                                updater = new Nesterovs(0.006, 0.9);
                                break;
                            case "a":
                                updater = new Adam(0.006);
                                break;
                        }
                        System.out.println("Enter number of epochs(default 10)");
                        MaxEpochs = Integer.parseInt(reader.readLine());
                        train();
                        System.out.println("Done!");
                    } catch (Exception e) {
                        System.out.println("Error");
                        System.out.println(e);
                    }
                }
                break;
                case "draw":
                    blblDraw.gogoDraw();
                    break;
                case "file": {
                    processOneImage(ImagePipe.processFile(null));
                }
                break;
                case "eval": {
                    DataSetIterator testIterator = ImagePipe.getTestIterator();
                    Evaluation eval = network.evaluate(testIterator);
                    System.out.println(eval.accuracy());
                    System.out.println(eval.precision());
                    System.out.println(eval.recall());
                }
                break;
                case "save": {
                    ModelSerializer.writeModel(network, reader.readLine() + ".zip", false);
                }
                break;
                case "load": {
                    try {
                        String path = reader.readLine();
                        if (path.equals("")) {
                            network = ModelSerializer.restoreMultiLayerNetwork("default.zip");
                        } else {
                            network = ModelSerializer.restoreMultiLayerNetwork(path + ".zip");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                break;
            }
        }
    }


    static MultiLayerNetwork train() throws IOException {
        DataSetIterator trainIterator = ImagePipe.getTrainIterator();
        train = trainIterator;
        DataSetIterator testIterator = ImagePipe.getTestIterator();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(ImagePipe.getTrainData())));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(ImagePipe.getTestData())));

        //building new NN config
        MultiLayerConfiguration config = BuildConfig(l1, l2, updater);
        // init network
        network = new MultiLayerNetwork(config);
        network.init();
        // trainer stopper
        EarlyStoppingConfiguration esConfig = new EarlyStoppingConfiguration.Builder()
                .epochTerminationConditions(new MaxEpochsTerminationCondition(MaxEpochs))
                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(30, TimeUnit.MINUTES))
                .scoreCalculator(new DataSetLossCalculator(testIterator, true))
                .evaluateEveryNEpochs(1)
                .modelSaver(new LocalFileModelSaver(System.getProperty("user.dir")))
                .build();

        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConfig, network, trainIterator);
        EarlyStoppingResult result = trainer.fit();
        System.out.println("Termination reason: " + result.getTerminationReason());
        System.out.println("Termination details: " + result.getTerminationDetails());
        System.out.println("Total epochs: " + result.getTotalEpochs());
        System.out.println("Best epoch number: " + result.getBestModelEpoch());
        System.out.println("Score at best epoch: " + result.getBestModelScore());

        return network;
    }

    static MultiLayerConfiguration BuildConfig(int l1, int l2, IUpdater updater) {
        MultiLayerConfiguration config;
        if (l2 > 0) {
            config = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(updater)
                    .l2(1e-4)
                    .list()
                    .layer(new DenseLayer.Builder()
                            .nIn(width * height)
                            .nOut(l1)
                            .activation(Activation.RELU)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .layer(new DenseLayer.Builder()
                            .nIn(l1)
                            .nOut(l2)
                            .activation(Activation.RELU)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nIn(l2)
                            .nOut(output)
                            .activation(Activation.SOFTMAX)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .pretrain(false)
                    .backprop(true)
                    .setInputType(InputType.convolutional(height, width, channels))
                    .build();
        } else {
            config = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(updater)
                    .l2(1e-4)
                    .list()
                    .layer(new DenseLayer.Builder()
                            .nIn(width * height)
                            .nOut(l1)
                            .activation(Activation.RELU)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nIn(l2)
                            .nOut(output)
                            .activation(Activation.SOFTMAX)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .pretrain(false)
                    .backprop(true)
                    .setInputType(InputType.convolutional(height, width, channels))
                    .build();
        }
        return config;
    }

    static public void processOneImage(INDArray img) {
        INDArray output = network.output(img);
        double[] arr = new double[(int) output.length()];
        for (int i = 0; i < output.length(); i++) {
            arr[Integer.parseInt(ImagePipe.getLables().get(i))] = output.getDouble(i);
        }
        double[] tmparr = Arrays.copyOf(arr, arr.length);
        for (int i = 0; i < 3; i++) {
            int no = 0;
            double max = Double.MIN_VALUE;
            for (int j = 0; j < tmparr.length; j++) {
                if (tmparr[j] > max) {
                    no = j;
                    max = tmparr[j];
                }
            }
            tmparr[no] = 0;
            max = Double.parseDouble(String.format("%.3f", max * 100));  // can be required precision
            System.out.println(no + " " + (max) + "%");
        }
        System.out.println(output.toString());
        System.out.println(ImagePipe.getLables());
    }
}
