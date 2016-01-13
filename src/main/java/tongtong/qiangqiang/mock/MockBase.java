package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;

import java.time.LocalDate;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.TICK;
import static tongtong.qiangqiang.data.H.bars;
import static tongtong.qiangqiang.data.H.ticks;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public abstract class MockBase {

    String security;

    TimeFrame resolution;

    LocalDate start, end;

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
        }
        else
            data = bars(security, resolution, start, end);
        for (int i=0; i<data.size(); i++)
            onData(data.get(i), i);
    }

    abstract void init();

    abstract void onData(BaseData data, int index);

    abstract void onComplete();

    /**
     * trade
      */
    protected boolean LONG = false;

    protected boolean SHORT = false;

    protected double longPrice = 0;

    protected double shortPrice = 0;

    protected double longDiff = 0;

    protected double shortDiff = 0;

    protected int longTime = 0;

    protected int shortTime = 0;

    protected boolean buyOpen(double price){
        if (!LONG){
            LONG = true;
            longPrice = price;
            longTime++;
            return true;
        }
        return false;
    }

    protected boolean sellClose(double price){
        if (LONG){
            LONG = false;
            System.out.println("原来利润: "+longDiff+", 增加利润："+(price-longPrice));
            longDiff += price - longPrice;
            return true;
        }
        return false;
    }

    protected boolean sellOpen(double price){
        if(!SHORT){
            SHORT = true;
            shortPrice = price;
            shortTime++;
            return true;
        }
        return false;
    }

    protected boolean buyClose(double price){
        if(SHORT){
            SHORT = false;
            System.out.println("原来利润: "+shortDiff+", 增加利润："+(shortPrice - price));
            shortDiff += shortPrice - price;
            return true;
        }
        return false;
    }
}
