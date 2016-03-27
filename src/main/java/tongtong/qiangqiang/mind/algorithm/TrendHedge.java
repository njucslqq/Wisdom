package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.factor.composite.ADX;
import cn.quanttech.quantera.common.factor.single.indicators.EMA;
import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.BaseData;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/3/14.
 */
public class TrendHedge extends Algorithm {

    private ADX adx = new ADX(14);

    private EMA pema = new EMA(14);

    private EMA nema = new EMA(14);

    public final String security = "m1609";

    public final int share = 1;

    public final double slipage = 0.0;

    public TrendHedge(String name, double commision, Pusher trader) {
        super(name, commision, trader);
    }

    @Override
    public void init() {
        setSecurity("m1609");
        setResolution(TimeFrame.MIN_5);
        setStart(LocalDate.of(2016, 1, 1));
        setEnd(LocalDate.of(2016, 3, 7));
        setVerbose(true);
    }

    @Override
    public void onData(BaseData data) {
        BarInfo bar = (BarInfo) data;
        double price = bar.close;

        adx.update(bar);
        double p = pema.update(adx.posDI.last(0));
        double n = nema.update(adx.negDI.last(0));

        int size = 256;
        visPrice(size,pema, nema, adx.adx, adx.posDI, adx.negDI);

        if (p > n) {
            buyClose(price + slipage);
            buy( price + slipage);
        } else {
            sell(price - slipage);
            sellOpen(price - slipage);
        }

        visProfit(size, profit());
    }

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.INTRA);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        List<Algorithm> algorithms = new ArrayList<>();
        algorithms.add(new TrendHedge("Trend", 0.2, pusher));

        new AlgorithmManager(algorithms).vis();
    }
}
