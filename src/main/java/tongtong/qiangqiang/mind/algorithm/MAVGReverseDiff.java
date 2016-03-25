package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.factor.Mavg;
import cn.quanttech.quantera.common.factor.composite.DEMA;
import cn.quanttech.quantera.common.factor.single.indicators.EMA;
import cn.quanttech.quantera.common.factor.single.indicators.RAW;
import cn.quanttech.quantera.common.factor.single.indicators.SMA;
import cn.quanttech.quantera.common.factor.single.indicators.WMA;
import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.BaseData;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.common.factor.MavgFactory.create;
import static cn.quanttech.quantera.common.type.data.TimeFrame.MIN_1;
import static org.apache.commons.lang3.tuple.Pair.of;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MavgReverseDiff extends Algorithm {

    public final RAW close;

    public final Mavg fast;

    public final Mavg slow;

    public final String security;

    public final TimeFrame resolution;

    public final LocalDate begin;

    public final LocalDate end;

    public final double slipage;

    public final double stopPoint;

    public MavgReverseDiff(String prefix, Pusher trader, double commision, String security, TimeFrame resolution, LocalDate begin, LocalDate end, Mavg fast, Mavg slow, double slipage, double stopPoint) {
        super(prefix + " - " + fast.getName() + " - " + slow.getName(), commision, trader);
        this.security = security;
        this.begin = begin;
        this.end = end;
        this.fast = fast;
        this.slow = slow;
        this.resolution = resolution;
        this.slipage = slipage;
        this.stopPoint = stopPoint;
        this.close = new RAW();
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(begin);
        setEnd(end);
        setVerbose(true);
        setShare(1);

        //setModel(MindType.Model.TRADE);
        //setState(MindType.State.REAL);
    }

    @Override
    public void onData(BaseData data) {
        BarInfo bar = (BarInfo) data;
        double price = bar.close;

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
        if (lfp(price) < -stopPoint)
            sellSilent(price - slipage);

        if (sfp(price) < -stopPoint)
            buyCloseSilent(price + slipage);

        visProfit(of("return", profit()));
    }

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.INTRA);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        List<Algorithm> p1 = portfolio1(pusher);
        new AlgorithmManager(p1).vis();
    }

    private static List<Algorithm> portfolio2(Pusher pusher) {
        int period = 17;
        String security = "rb1610";
        LocalDate begin = LocalDate.of(2016, 3, 10);
        LocalDate end = LocalDate.of(2016, 3, 25);
        TimeFrame resolution = MIN_1;

        Class<?>[] c = {SMA.class, EMA.class, WMA.class, DEMA.class};
        List<Algorithm> algorithms = new ArrayList<>();
        for (int i = 1; i < c.length; i++)
            for (int j = 0; j < i; j++) {
                Mavg fast = create(c[i], period);
                Mavg slow = create(c[j], period);
                algorithms.add(new MavgReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow, 1.0, 12.0));
            }

        return algorithms;
    }

    private static List<Algorithm> portfolio1(Pusher pusher) {
        int period = 17;
        String security = "";
        Mavg fast = null;
        Mavg slow = null;
        TimeFrame resolution = TimeFrame.MIN_1;
        LocalDate begin = LocalDate.of(2016, 3, 10);
        LocalDate end = LocalDate.of(2016, 3, 25);

        List<Algorithm> algorithms = new ArrayList<>();

        /**
         * add two algorithms for bu1606
         */
        security = "bu1606";
        fast = new WMA(period);
        slow = new SMA(period);
        //algorithms.add(new MavgReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow, 2.0, 12.0));

        fast = new DEMA(period);
        slow = new SMA(period);
        //algorithms.add(new MavgReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow, 2.0, 12.0));

        /**
         * add two algorithms for m1609
         */
        security = "rb1610";
        fast = new WMA(period);
        slow = new SMA(period);
        //algorithms.add(new MavgReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow, 1.0, 14.0));

        fast = new DEMA(period);
        slow = new WMA(period);
        //algorithms.add(new MavgReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow, 1.0, 14.0));

        fast = new EMA(period);
        slow = new SMA(period);
        algorithms.add(new MavgReverseDiff(security, pusher, 0.2, security, resolution, begin, end, fast, slow, 1.0, 12.0));

        return algorithms;
    }
}
