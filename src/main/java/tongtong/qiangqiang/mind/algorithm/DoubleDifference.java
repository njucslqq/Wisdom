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

import static cn.quanttech.quantera.common.factor.MavgFactory.*;
import static cn.quanttech.quantera.common.type.data.TimeFrame.MIN_30;
import static org.apache.commons.lang3.tuple.Pair.of;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/3/19.
 */
public class DoubleDifference extends Algorithm {

    public final RAW close;

    public final Mavg fast;

    public final Mavg slow;

    public final RAW dif;

    public final String security;

    public final TimeFrame resolution;

    public final LocalDate begin;

    public final LocalDate end;

    public final double stopPoint;

    public final double slipage = 1.0;

    public final Mavg fast_fast;

    public final Mavg slow_slow;

    public final RAW dif_dif;

    public DoubleDifference(String prefix, Pusher trader, double commision, double stopPoint, String security, TimeFrame resolution, LocalDate begin, LocalDate end, Mavg fast, Mavg slow) {
        super(prefix + " - " + fast.getName() + " - " + slow.getName(), commision, trader);
        this.security = security;
        this.begin = begin;
        this.end = end;
        this.fast = fast;
        this.slow = slow;
        this.dif = new RAW();
        this.resolution = resolution;
        this.stopPoint = stopPoint;
        this.close = new RAW();
        this.fast_fast = create(fast.getClass(), fast.getPeriod());
        this.slow_slow = create(slow.getClass(), slow.getPeriod());
        this.dif_dif = new RAW();
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
        double d = dif.update(f-s);

        double ff = fast_fast.update(d);
        double ss = slow_slow.update(d);
        dif_dif.update(ff-ss);

        int size = 256;
        visPrice(size, fast, slow, close);

        if (dif_dif.size()>1) {
            if (dif_dif.last(0) > dif_dif.last(1)) {
                buyClose(price + slipage);
                buy(price + slipage);
            } else {
                sell(price - slipage);
                sellOpen(price - slipage);
            }
        }

        //visProfit(size, dif_dif);

        visProfit(size, profit());

        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        List<Algorithm> p1 = portfolio1(pusher);
        new AlgorithmManager(p1).vis();
    }

    private static List<Algorithm> portfolio1(Pusher pusher) {
        int period = 51;
        String security = "m1609";
        LocalDate begin = LocalDate.of(2015, 6, 10);
        LocalDate end = LocalDate.of(2016, 3, 19);
        TimeFrame resolution = MIN_30;

        Class<?>[] c = {SMA.class, EMA.class, WMA.class, DEMA.class};
        List<Algorithm> algorithms = new ArrayList<>();
        for (int i = 1; i < c.length; i++)
            for (int j = 0; j < i; j++) {
                Mavg fast = create(c[i], period);
                Mavg slow = create(c[j], period);
                algorithms.add(new DoubleDifference(security, pusher, 0.2, 12.0, security, resolution, begin, end, fast, slow));
            }

        return algorithms;
    }
}
