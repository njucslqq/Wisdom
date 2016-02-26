package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.TICK;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.data.Historical.ticks;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public abstract class MockBase extends TradeBase {

    protected String security;

    protected TimeFrame resolution;

    protected LocalDate start, end;

    protected final List<Double> profit = new ArrayList<>();

    protected final TimeSeriesChart profitChart;

    public MockBase (String name){
        profitChart  = new TimeSeriesChart(name);
    }

    protected void setSecurity(String security) {
        this.security = security;
    }

    protected void setResolution(TimeFrame resolution) {
        this.resolution = resolution;
    }

    protected void setStart(LocalDate start) {
        this.start = start;
    }

    protected void setEnd(LocalDate end) {
        this.end = end;
    }

    public void simulate() {
        List<? extends BaseData> data = null;
        if (resolution.equals(TICK)) {
            data = ticks(security, start, end);
            data.remove(0);
        } else
            data = bars(security, resolution, start, end);

        for (int i = 0; i < data.size(); i++) {
            onData(data.get(i), i);
            profit.add(lDif + sDif);
        }
    }

    public void onComplete(){
        System.out.println("Long  Profit: " + lDif);
        System.out.println("Short Profit: " + sDif);
        profitChart.vis("HH:mm:ss", profit);
    }

    public abstract void init();

    public abstract void onData(BaseData data, int index);
}
