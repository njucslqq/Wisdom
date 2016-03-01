package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.MindType;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.time.LocalDate;

import static tongtong.qiangqiang.mind.MindType.Model.TRADE;
import static tongtong.qiangqiang.mind.MindType.State.MOCK;
import static tongtong.qiangqiang.mind.MindType.State.REAL;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MAVGDiff extends Algorithm {

    public final MAVG fast;

    public final MAVG slow;

    public final TimeFrame resolution;

    public final String security;

    public final LocalDate begin;

    public final int share = 1;

    public final double slipage = 0.0;

    public final Intermediate close = new Intermediate();

    private final TimeSeriesChart tsc;

    public MAVGDiff(String prefix, String security, TimeFrame resolution, LocalDate begin, MAVG fast, MAVG slow) {
        super(prefix + " - " + fast.name() + " - " + slow.name());
        this.security = security;
        this.begin = begin;
        this.fast = fast;
        this.slow = slow;
        this.resolution = resolution;
        tsc = new TimeSeriesChart(fast.name() + " - " + slow.name());
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(begin);
        setEnd(LocalDate.now());
        setVerbose(true);
        setModel(TRADE);
        setState(REAL);
    }

    @Override
    public void onData(BaseData data, int index) {
        BarInfo bar = (BarInfo) data;
        double price = bar.closePrice;
        close.update(price);
        double f = fast.update(price);
        double s = slow.update(price);

        int size = 128;
        if (close.dataSize() < size) {
            tsc.vis("HH:mm:ss", fast.primary().data.all(), slow.primary().data.all(), close.primary().data.all());
        } else {
            tsc.vis("HH:mm:ss", fast.last(size), slow.last(size), close.last(size));
        }

        if (f < s) {
            buyClose(security, share, price + slipage);
            buy(security, share, price + slipage);
        } else {
            sell(security, share, price - slipage);
            sellOpen(security, share, price - slipage);
        }
    }

    @Override
    public void onComplete() {
        conclude();
        int size = 128;
        //tsc.vis("HH:mm:ss", fast.last(size), slow.last(size), close.last(size));
    }
}
