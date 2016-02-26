package tongtong.qiangqiang.data.factor.single.indicators;

import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.data.factor.single.SingleIndicator;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;


/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/1/30.
 */
public class WMA extends SingleIndicator<Double> implements MovingAverage {

    public final List<Double> weights;

    public final int period;

    public WMA(int period) {
        this(weights(period));
    }

    public WMA(List<Double> weights) {
        this(weights, MAX_VALUE);
    }

    public WMA(List<Double> weights, int cacheSize) {
        super(cacheSize, weights.size());
        this.weights = weights;
        this.period = weights.size();
    }

    @Override
    public double update(Double input) {
        previous.add(input);
        if (previous.size() < period)
            data.add(wma(previous.window, weights(previous.size())));
        else
            data.add(wma(previous.window, weights));
        return data.getLast();
    }

    @Override
    public String name() {
        return "WMA[" + period + "]";
    }

    public static List<Double> weights(int N) {
        List<Double> res = new ArrayList<>();
        double sum = N * (N + 1) / 2.0;
        for (int i = 1; i <= N; i++)
            res.add(i / sum);
        return res;
    }

    public static double wma(List<Double> value, List<Double> weight) {
        assert value.size() == weight.size();
        double sum = 0.;
        for (int i = 0; i < value.size(); i++)
            sum += value.get(i) * weight.get(i);
        return sum;
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
