package tongtong.qiangqiang.mind.order;

import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/27.
 */
public abstract class BaseOrder implements IOrder {

    protected boolean lPos = false;

    protected boolean sPos = false;

    protected double lPrice = 0.;

    protected double sPrice = 0.;

    protected double lDif = 0.;

    protected double sDif = 0.;

    protected int lTime = 0;

    protected int sTime = 0;

    protected final List<Double> profit = new ArrayList<>();

    protected final TimeSeriesChart profitChart;

    public BaseOrder(String name) {
        profitChart = new TimeSeriesChart(name);
    }
}
