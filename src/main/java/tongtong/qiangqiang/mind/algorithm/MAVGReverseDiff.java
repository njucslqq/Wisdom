package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.MAVGFactory;
import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.composite.MACD;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.MindType;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static org.apache.commons.lang3.tuple.Pair.of;
import static tongtong.qiangqiang.mind.MindType.Model.TRADE;
import static tongtong.qiangqiang.mind.MindType.State.REAL;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MAVGReverseDiff extends Algorithm {

    public final MAVG fast;

    public final MAVG slow;

    public final TimeFrame resolution;

    public final String security;

    public final LocalDate begin;

    public final int share = 1;

    public final double slipage = 0.0;

    public final Intermediate close = new Intermediate();

    public MAVGReverseDiff(String prefix, Pusher trader, String security, TimeFrame resolution, LocalDate begin, MAVG fast, MAVG slow) {
        super(prefix + " - " + fast.getName() + " - " + slow.getName(), trader);
        this.security = security;
        this.begin = begin;
        this.fast = fast;
        this.slow = slow;
        this.resolution = resolution;
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(begin);
        setEnd(LocalDate.now());
        setVerbose(true);
        //setModel(TRADE);
        //setState(REAL);
    }

    @Override
    public void onData(BaseData data) {
        BarInfo bar = (BarInfo) data;
        double price = bar.closePrice;
        close.update(price);
        double f = fast.update(price);
        double s = slow.update(price);

        int size = 128;
        if (close.size() < size) {
            visPrice(of(fast.getName(), fast.all()), of(slow.getName(), slow.all()), of("close", close.all()));
        } else {
            visPrice(of(fast.getName(), fast.lastn(size)), of(slow.getName(), slow.lastn(size)), of("close", close.lastn(size)));
        }

        if (fast.size() > 17) {
            if (f < s) {
                buyClose(security, share, price + slipage);
                buy(security, share, price + slipage);
            } else {
                sell(security, share, price - slipage);
                sellOpen(security, share, price - slipage);
            }
        }

        visProfit(of("return", profit()));
    }

    public static void main(String[] args) {
        setNetDomain(CONST.INTRA_QUANDIS_URL);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        int period = 17;
        String security = "m1609";
        TimeFrame resolution = MIN_1;
        LocalDate begin = LocalDate.of(2016, 3, 9);

        List<Algorithm> algorithms = new ArrayList<>();

        Class<?>[] clazz = {SMA.class, EMA.class, WMA.class, DEMA.class};
        for (int i = 1; i < clazz.length; i++)
            for (int j = 0; j < i; j++) {
                MAVG fast = MAVGFactory.create(clazz[i], period);
                MAVG slow = MAVGFactory.create(clazz[j], period);
                algorithms.add(new MAVGReverseDiff("[" + i + "," + j + "]", pusher, security, resolution, begin, fast, slow));
            }


        //MAVG fast = new WMA(period);
        //MAVG slow = new SMA(period);
        //algorithms.add(new MAVGReverseDiff("Diff-" + security + "-", pusher, security, resolution, begin, fast, slow));
        new AlgorithmManager(algorithms).vis();
    }
}
