package tongtong.qiangqiang.hunt.rnn;

import cn.quanttech.quantera.common.datacenter.HistoricalData;
import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.TimeFrame;
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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/5/1.
 */
public class RNN {

    public static void main(String[] args) {
        int lstmLayerSize = 200;                    //Number of units in each GravesLSTM layer
        int miniBatchSize = 15;                        //Size of mini batch to use when  training
        int exampleLength = 500;                    //Length of each training example sequence to use. This could certainly be increased
        int tbpttLength = 50;                       //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        int numEpochs = 100;                            //Total number of training epochs
        int sampleLength = 10;                      // number of bars to predict
        int generateSamplesEveryNMinibatches = 5;  //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        Random rng = new Random(12345);

        BarIterator iter = getBarIterator(miniBatchSize, exampleLength, rng);
        int nOut = iter.totalOutcomes();

        //Set up network configuration:
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(3)
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
        net.setListeners(new ScoreIterationListener(1));//, new HistogramIterationListener(1)));

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
            while (iter.hasNext()) {
                DataSet ds = iter.next();
                net.fit(ds);
                if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                    System.out.println("--------------------");
                    System.out.println("Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize + "x" + exampleLength + " bars");
                    BarInfo dif = new BarInfo(null, null, null, null,null);
                    dif.open = 1;
                    dif.high = 2;
                    dif.low = 2;
                    dif.close = 2;
                    dif.volume = 0;
                    List<BarInfo> predicts = predict(dif, net, iter, sampleLength);

                    BarInfo begin = new BarInfo(null,null,null,null,null);
                    begin.open = 2100;
                    begin.high = 2104;
                    begin.low = 2080;
                    begin.close = 2101;

                    toShow(begin, predicts);
                }
            }

            iter.reset();    //Reset iterator for another epoch
        }

        System.out.println("\n\nExample complete");
    }

    public static BarIterator getBarIterator(int miniBatchSize, int exampleLength, Random rng) {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);

        List<BarInfo> bars = HistoricalData.bars("rb1605", TimeFrame.MIN_1, LocalDate.of(2015, 6, 1), LocalDate.of(2016, 6, 1));
        ArrayList<BarInfo> barDiff = new ArrayList<>();
        for (int i = 1; i < bars.size(); i++) {
            BarInfo pre = bars.get(i - 1);
            BarInfo cur = bars.get(i);
            BarInfo b = new BarInfo(null, null, null, null, null);
            b.open = cur.open - pre.open;
            b.high = cur.high - pre.high;
            b.low = cur.low - pre.low;
            b.close = cur.close - pre.close;
            b.volume = 0;//cur.volume - pre.volume;
            barDiff.add(b);
        }

        return new BarIterator(barDiff, miniBatchSize, exampleLength, rng);
    }

    public static List<BarInfo> predict(BarInfo begin, MultiLayerNetwork net, BarIterator iter, int sampleLength){
        net.rnnClearPreviousState();

        List<BarInfo> res = new ArrayList<>();
        INDArray input = getInput(begin, iter);
        for (int i=0; i<sampleLength;i++){
            INDArray output = net.rnnTimeStep(input);
            BarInfo dif = new BarInfo(null, null, null, null,null);
            dif.open = output.getDouble(new int[]{0, 0});
            dif.high = output.getDouble(new int[]{0, 1});
            dif.low = output.getDouble(new int[]{0, 2});
            dif.close = output.getDouble(new int[]{0, 3});
            dif.volume = 0;//(int)output.getDouble(new int[]{0, 4});
            res.add(dif);
            input = getInput(dif, iter);
        }
        return res;
    }

    private static INDArray getInput(BarInfo begin, BarIterator iter){
        INDArray input = Nd4j.zeros(1, iter.inputColumns());
        input.putScalar(new int[]{0, 0}, begin.open);
        input.putScalar(new int[]{0, 1}, begin.high);
        input.putScalar(new int[]{0, 2}, begin.low);
        input.putScalar(new int[]{0, 3}, begin.low);
        input.putScalar(new int[]{0, 4}, begin.volume);
        return input;
    }

    private static void toShow(BarInfo begin, List<BarInfo> dif){
        BarInfo pre = new BarInfo(null,null,null,null,null);
        pre.open=begin.open;
        pre.high=begin.high;
        pre.low=begin.low;
        pre.close=begin.close;

        LocalDate startDate = LocalDate.of(2016, 5, 2);
        for (BarInfo d : dif){
            BarInfo cur = new BarInfo(null,null,null,null,null);
            cur.open = pre.open+d.open;
            cur.high = pre.high+d.high;
            cur.low = pre.low+d.low;
            cur.close = pre.close+d.close;

            StringBuilder line = new StringBuilder();
            line.append("['"+startDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))+"',");
            line.append(cur.open).append(",");
            line.append(cur.close).append(",");
            line.append(cur.low).append(",");
            line.append(cur.high).append("],");
            System.out.println(line);
            pre = cur;
            startDate=startDate.plusDays(1);
        }
    }
}
