package tongtong.qiangqiang.mind.order;

import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.util.ArrayList;
import java.util.LinkedList;
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

    protected final LinkedList<Double> longProfit = new LinkedList<>();

    protected final LinkedList<Double> shortProfit = new LinkedList<>();

    protected final LinkedList<Double> profit = new LinkedList<>();

    protected final TimeSeriesChart profitChart;

    public BaseOrder(String name) {
        profitChart = new TimeSeriesChart(name);
    }

    protected boolean buyAction(double price) {
        if (!lPos) {
            lPos = true;
            lPrice = price;
            lTime++;
            return true;
        }
        return false;
    }

    protected boolean sellAction(double price) {
        if (lPos) {
            lPos = false;
            double delta = (price - lPrice) - 0.2;
            lDif += delta;
            longProfit.add(delta);
            profit.add(lDif + sDif);
            return true;
        }
        return false;
    }

    protected boolean buyCloseAction(double price) {
        if (sPos) {
            sPos = false;
            double delta = (sPrice - price) - 0.2;
            sDif += delta;
            shortProfit.add(delta);
            profit.add(lDif + sDif);
            return true;
        }
        return false;
    }

    protected boolean sellOpenAction(double price) {
        if (!sPos) {
            sPos = true;
            sPrice = price;
            sTime++;
            return true;
        }
        return false;
    }

    @Override
    public double longReturn() {
        return lDif;
    }

    @Override
    public double shortReturn() {
        return sDif;
    }

    @Override
    public double totalReturn() {
        return lDif + sDif;
    }

    @Override
    public List<Double> profit() {
        return profit;
    }

    @Override
    public void conclude() {
        System.out.println("\n<========== Summary ==========>");
        System.out.println("Long  Profit: " + lDif);
        System.out.println("Short Profit: " + sDif);
        System.out.println("Long  Trading Time: " + lTime);
        System.out.println("Short Trading Time: " + sTime);
        System.out.println("\n<==========   End   ==========>");
        profitChart.vis("HH:mm:ss", profit);
    }
}
