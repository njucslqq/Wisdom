package tongtong.qiangqiang.hunt.trend;

import cn.quanttech.quantera.common.factor.composite.MACD;
import cn.quanttech.quantera.common.factor.single.indicators.EMA;
import cn.quanttech.quantera.common.factor.single.indicators.RAW;
import cn.quanttech.quantera.common.type.quotation.BarInfo;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies5;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;
import tongtong.qiangqiang.hunt.LearnDirection;

import java.util.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/5/1.
 */
public class BarIterator implements DataSetIterator {

    public final ArrayList<BarInfo> bars;

    public final int miniBatchSize;

    public final int exampleLength;

    public final Random rng;

    private int numExamples;

    private int inputColums;

    private LinkedList<Integer> exampleStartOffsets = new LinkedList<>();

    private EMA ema1 = new EMA(13);

    private EMA ema2 = new EMA(27);

    private EMA ema3 = new EMA(45);

    private MACD macd = new MACD();

    private RAW mtm = new RAW();

    private RAW c2o = new RAW();

    private List<RAW> diffs = new ArrayList<>();

    private List<LearnDirection.Direction> dirs;

    public BarIterator(ArrayList<BarInfo> bars, int miniBatchSize, int exampleLength, Random rng) {
        this.bars = bars;
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        this.rng = rng;

        numExamples = bars.size() / exampleLength;
        for (int i = 0; i < numExamples; i++)
            exampleStartOffsets.add(i * exampleLength);

        Collections.shuffle(exampleStartOffsets, rng);

        init();
    }

    public void init() {
        List<Double> close = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            ema1.update(bars.get(i).close);
            ema2.update(bars.get(i).close);
            ema3.update(bars.get(i).close);
            macd.update(bars.get(i).close);
            mtm.update(i == 0 ? 0.0 : bars.get(i).close - bars.get(i - 1).close);
            c2o.update(bars.get(i).close - bars.get(i).open);
            close.add(bars.get(i).close);
        }

        diffs.add(ema1.derivative(1));
        diffs.add(ema2.derivative(1));
        diffs.add(ema3.derivative(1));
        diffs.add(macd.DIF.derivative(1));
        diffs.add(macd.DEA.derivative(1));
        diffs.add(mtm);
        diffs.add(c2o);

        this.inputColums = diffs.size();

        int size = 512;
        int top = 3;
        int gap = 25;
        Transform t = new Transform(new FastWaveletTransform(new Daubechies5()));
        LearnDirection.WaveletConfig config = new LearnDirection.WaveletConfig(t, size, top, gap);
        dirs = LearnDirection.judgeDirection(close, config).direction;
    }

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.isEmpty())
            throw new NoSuchElementException("no examples left");

        int currMiniBatchSize = Math.min(num, exampleStartOffsets.size());

        INDArray input = Nd4j.zeros(currMiniBatchSize, inputColums, exampleLength);
        INDArray label = Nd4j.zeros(currMiniBatchSize, 2, exampleLength);

        for (int i = 0; i < currMiniBatchSize; i++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;
            int c = 0;
            for (int j = startIdx; j < endIdx; j++, c++) {
                for (int h = 0; h<diffs.size();h++){
                    input.putScalar(new int[]{i, h, c}, diffs.get(h).first(j));
                }

                label.putScalar(new int[]{i, dirs.get(j).value(), c}, 1);
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
        return totalExamples() - exampleStartOffsets.size();
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
