package tongtong.qiangqiang.mind.order;

import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MockOrder implements Order {

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

    public MockOrder (String name){
        profitChart  = new TimeSeriesChart(name);
    }

    @Override
    public boolean buy(String id, int share, double price) {
        if (!lPos) {
            lPos = true;
            lPrice = price;
            lTime++;
            //System.out.println("\n[long  open]: " + price);
            profit.add(lDif + sDif);
            return true;
        }
        return false;
    }

    @Override
    public boolean sell(String id, int share, double price) {
        if (lPos) {
            lPos = false;
            lDif += price - lPrice;
            //System.out.println("[long close]: " + price + ", delta: " + (price - lPrice) + ", profit: " + lDif);
            profit.add(lDif + sDif);
            profitChart.vis("HH:mm:ss", profit);
            return true;
        }
        return false;
    }

    @Override
    public boolean buyClose(String id, int share, double price) {
        if (sPos) {
            sPos = false;
            sDif += sPrice - price;
            //System.out.println("[short close]: " + price + ", delta: " + (sPrice - price) + ", profit: " + sDif);
            profit.add(lDif + sDif);
            profitChart.vis("HH:mm:ss", profit);
            return true;
        }
        return false;
    }

    @Override
    public boolean sellOpen(String id, int share, double price) {
        if (!sPos) {
            sPos = true;
            sPrice = price;
            sTime++;
            //System.out.println("\n[short open]: " + price);
            profit.add(lDif + sDif);
            return true;
        }
        return false;
    }

    @Override
    public void conclude() {
        System.out.println("\nLong  Profit: " + lDif);
        System.out.println("Short Profit: " + sDif);
        profitChart.vis("HH:mm:ss", profit);
    }
}
