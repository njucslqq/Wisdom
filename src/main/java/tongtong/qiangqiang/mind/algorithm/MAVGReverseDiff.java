package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.composite.DEMA;
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

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MAVGReverseDiff extends Algorithm {

    public final Intermediate close;

    public final MAVG fast;

    public final MAVG slow;

    public final String security;

    public final TimeFrame resolution;

    public final LocalDate begin;

    public final LocalDate end;

    public final double slipage = 2.0;

    public MAVGReverseDiff(String prefix, Pusher trader, double commision, String security, TimeFrame resolution, LocalDate begin, LocalDate end, MAVG fast, MAVG slow) {
        super(prefix + " - " + fast.getName() + " - " + slow.getName(), commision, trader);
        this.security = security;
        this.begin = begin;
        this.end = end;
        this.fast = fast;
        this.slow = slow;
        this.resolution = resolution;
        this.close = new Intermediate();
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(begin);
        setEnd(end);
        setVerbose(true);
        setShare(1);

        setModel(MindType.Model.TRADE);
        setState(MindType.State.REAL);
    }

    @Override
    public void onData(BaseData data) {
        BarInfo bar = (BarInfo) data;
        double price = bar.closePrice;

        close.update(price);
        double f = fast.update(price);
        double s = slow.update(price);

        int size = 128;
        visPrice(size, fast, slow, close);

        if (f < s) {
            buyClose(price);
            buy(price);
        } else {
            sell(price);
            sellOpen(price);
        }
        /*
         rb1610  10 - 14
         bu1606  8 - 12
          */
        if (lfp(price) < -7.0)
            sellSilent(price - slipage);

        if (sfp(price) < -7.0)
            buyCloseSilent(price + slipage);

        visProfit(of("return", profit()));
    }

    public static void main(String[] args) {
        setNetDomain(CONST.INTRA_QUANDIS_URL);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        List<Algorithm> p1 = portfolio1(pusher);
        new AlgorithmManager(p1).vis();
    }

    private static List<Algorithm> portfolio1(Pusher pusher) {
        int period = 17;
        String security = "";
        MAVG fast = null;
        MAVG slow = null;
        TimeFrame resolution = MIN_1;
        LocalDate begin = LocalDate.of(2016, 1, 1);
        LocalDate end = LocalDate.of(2016, 3, 1);

        List<Algorithm> algorithms = new ArrayList<>();

        /**
         * add two algorithms for bu1606
         */
        security = "bu1606";
        fast = new WMA(period);
        slow = new SMA(period);
        algorithms.add(new MAVGReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow));

        fast = new DEMA(period);
        slow = new SMA(period);
        //algorithms.add(new MAVGReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow));

        /**
         * add two algorithms for m1609
         */
        security = "rb1610";
        fast = new WMA(period);
        slow = new SMA(period);
        //algorithms.add(new MAVGReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow));

        fast = new DEMA(period);
        slow = new WMA(period);
        algorithms.add(new MAVGReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow));

        return algorithms;
    }
}
