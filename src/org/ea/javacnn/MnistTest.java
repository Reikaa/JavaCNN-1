package org.ea.javacnn;

import org.ea.javacnn.data.DataBlock;
import org.ea.javacnn.data.OutputDefinition;
import org.ea.javacnn.data.TrainResult;
import org.ea.javacnn.layers.*;
import org.ea.javacnn.readers.MnistReader;
import org.ea.javacnn.trainers.AdaGradTrainer;
import org.ea.javacnn.trainers.Trainer;

import java.util.ArrayList;
import java.util.List;

/**
 * This a test network to try the network on the
 * [Mnist dataset](http://yann.lecun.com/exdb/mnist/)
 *
 * @author Daniel Persson (mailto.woden@gmail.com)
 */
public class MnistTest {

    public static void main(String[] argv) {
        String[] classes_txt = new String[] {"0","1","2","3","4","5","6","7","8","9"};
        String dataset_name = "mnist";
        int num_batches = 21; // 20 training batches, 1 test
        int test_batch = 20;
        int num_samples_per_batch = 3000;
        int image_dimension = 28;
        int image_channels = 1;
        boolean use_validation_data = true;
        boolean random_flip = false;
        boolean random_position = false;

        List<Layer> layers = new ArrayList<Layer>();
        OutputDefinition def = new OutputDefinition();
        layers.add(new InputLayer(def, 24, 24, 1));
        layers.add(new ConvolutionLayer(def, 5, 8, 1, 2));
        layers.add(new LocalResponseNormalizationLayer(def));
        layers.add(new PoolingLayer(def, 2, 2, 0));
        layers.add(new ConvolutionLayer(def, 5, 16, 1, 2));
        layers.add(new LocalResponseNormalizationLayer(def));
        layers.add(new PoolingLayer(def, 3,3, 0));
        layers.add(new FullyConnectedLayer(def, 10));
        layers.add(new SoftMaxLayer(def, 10));

        JavaCNN net = new JavaCNN(layers);
        Trainer trainer = new AdaGradTrainer(net, 20, 0.001f);

        MnistReader mr = new MnistReader("mnist/train-labels-idx1-ubyte", "mnist/train-images-idx3-ubyte");

        try {
            long start = System.currentTimeMillis();

            TrainResult tr = null;
            DataBlock db = new DataBlock(28, 28, 1, 0);
            for(int j = 1; j < 501; j++) {
                for (int i = 0; i < mr.size(); i++) {
                    db.addImageData(mr.readNextImage());
                    tr = trainer.train(db, mr.readNextLabel());
                    if (i % 1000 == 0) {
                        System.out.println((System.currentTimeMillis() - start) + " Pass " + j + " Read images: " + i);
                        System.out.println(tr.toString());
                        start = System.currentTimeMillis();
                    }
                }
                mr.reset();
            }
            System.out.println(tr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
