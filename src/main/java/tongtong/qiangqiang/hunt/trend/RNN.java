package tongtong.qiangqiang.hunt.trend;

import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import tongtong.qiangqiang.hunt.rnn.BarIterator;

import java.util.Random;

import static tongtong.qiangqiang.hunt.rnn.util.ModelUtil.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/5/1.
 */
public class RNN {

    static {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);
    }

    public static void main(String[] args) {
        int lstmLayerSize = 200;                        //Number of units in each GravesLSTM layer
        int miniBatchSize = 30;                         //Size of mini batch to use when  training
        int exampleLength = 1500;                       //Length of each training example sequence to use. This could certainly be increased
        int tbpttLength = 100;                          //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        int numEpochs = 165;                            //Total number of training epochs
        int sampleLength = 60;                          // number of bars to predict
        int generateSamplesEveryNMinibatches = 10;      //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        Random rng = new Random(12345);

        BarIterator iter = getBarIterator(miniBatchSize, exampleLength, rng);
        int nOut = iter.totalOutcomes();

        //Set up network configuration:
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .learningRate(0.1)
                .rmsDecay(0.95)
                .seed(12345)
                .regularization(true)
                .l2(0.001)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .list(3)
                .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                        .activation("tanh").build())
                .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation("tanh").build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE).activation("identity")
                        .nIn(lstmLayerSize).nOut(nOut).build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(tbpttLength)
                .tBPTTBackwardLength(tbpttLength)
                .pretrain(false)
                .backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));//Arrays.asList(new ScoreIterationListener(1), new HistogramIterationListener(1)));

        //Print the  number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            int nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);


        //Do training, and then generate and print samples from network
        int miniBatchNumber = 0;
        for (int i = 0; i < numEpochs; i++) {
            System.out.println("=========> epoch: " + i);
            while (iter.hasNext()) {
                DataSet ds = iter.next();
                net.fit(ds);
                if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                    System.out.println("--------------------");
                    System.out.println("Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize + "x" + exampleLength + " bars");
                    getPrediction(net, iter);
                }
            }
            iter.reset();    //Reset iterator for another epoch
        }

        saveModel(net);

        System.out.println("\n\nExample complete");
    }
}
