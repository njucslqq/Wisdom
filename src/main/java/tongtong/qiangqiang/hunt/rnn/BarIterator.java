package tongtong.qiangqiang.hunt.rnn;

import cn.quanttech.quantera.common.type.data.BarInfo;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/5/1.
 */
public class BarIterator implements DataSetIterator {

    public final ArrayList<BarInfo> barDiff;

    public final int miniBatchSize;

    public final int exampleLength;

    public final Random rng;

    private int numExamples;

    private int inputColums;

    private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

    public BarIterator(ArrayList<BarInfo> barDiff, int miniBatchSize, int exampleLength, Random rng) {
        this.barDiff = barDiff;
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        this.rng = rng;

        inputColums = 5;

        numExamples = barDiff.size() / exampleLength;
        for (int i = 0; i < numExamples; i++)
            exampleStartOffsets.add(i * exampleLength);
        Collections.shuffle(exampleStartOffsets, rng);
    }

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.isEmpty())
            throw new NoSuchElementException("no examples left");

        int currMiniBatchSize = Math.min(num, exampleStartOffsets.size());

        //Allocate space:
        //Note the order here:
        // dimension 0 = number of examples in minibatch
        // dimension 1 = size of each vector (i.e., number of characters)
        // dimension 2 = length of each time series/example

        // five input for: open, high, low, close and volume
        INDArray input = Nd4j.zeros(currMiniBatchSize, inputColums, exampleLength);
        INDArray label = Nd4j.zeros(currMiniBatchSize, inputColums, exampleLength);

        for (int i = 0; i < currMiniBatchSize; i++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;
            int c = 0;
            for (int j = startIdx; j < endIdx; j++, c++) {
                input.putScalar(new int[]{i, 0, c}, barDiff.get(j).open);
                input.putScalar(new int[]{i, 1, c}, barDiff.get(j).high);
                input.putScalar(new int[]{i, 2, c}, barDiff.get(j).low);
                input.putScalar(new int[]{i, 3, c}, barDiff.get(j).close);
                input.putScalar(new int[]{i, 4, c}, barDiff.get(j).volume);

                label.putScalar(new int[]{i, 0, c}, barDiff.get(j + 1).open);
                label.putScalar(new int[]{i, 1, c}, barDiff.get(j + 1).high);
                label.putScalar(new int[]{i, 2, c}, barDiff.get(j + 1).low);
                label.putScalar(new int[]{i, 3, c}, barDiff.get(j + 1).close);
                label.putScalar(new int[]{i, 4, c}, barDiff.get(j + 1).volume);
            }
        }

        return new DataSet(input, label);
    }

    @Override
    public int totalExamples() {
        return numExamples;
    }

    @Override
    public int inputColumns() {
        return inputColums;
    }

    @Override
    public int totalOutcomes() {
        return inputColums;
    }

    @Override
    public void reset() {
        exampleStartOffsets.clear();
        for (int i = 0; i < numExamples; i++)
            exampleStartOffsets.add(i * exampleLength);
        Collections.shuffle(exampleStartOffsets, rng);
    }

    @Override
    public int batch() {
        return miniBatchSize;
    }

    @Override
    public int cursor() {
        return totalExamples()-exampleStartOffsets.size();
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean hasNext() {
        return exampleStartOffsets.size() > 0;
    }

    @Override
    public DataSet next() {
        return next(miniBatchSize);
    }
}
