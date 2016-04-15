package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.factor.single.indicators.RAW;
import cn.quanttech.quantera.common.factor.single.indicators.WMA;
import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.BaseData;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-22.
 */
public class BarAnalysis extends Algorithm {

    public final RAW close = new RAW();

    public final WMA wma = new WMA(7);

    public final ArrayList<BarInfo> bars = new ArrayList<>();

    private double slippage = 1.0;

    public BarAnalysis(Pusher pusher) {
        super("BarAnalysis", 0.2, pusher);
    }

    @Override
    public void init() {
        setSecurity("rb1605");
        setResolution(TimeFrame.MIN_10);
        setStart(LocalDate.of(2015, 6, 10));
        setEnd(LocalDate.of(2016, 3, 1));
        setVerbose(true);
        setShare(1);
    }

    @Override
    public void onData(BaseData data) {
        BarInfo bar = (BarInfo) data;
        double tp = (bar.high + bar.close + bar.low) / 3.0;

        close.update(bar.close);
        wma.update(tp);
        bars.add(bar);

        double gap = 1;
        if (bars.size() > 1) {
            BarInfo pre = bars.get(bars.size() - 2);
            if (pBar(pre, gap) && pBar(bar, gap)) {
                //buyClose(bar.close + slippage);
                buy(bar.close + slippage);
            } else if (nBar(pre, 0) && nBar(bar, 0)) {
                sell(bar.close - slippage);
                //sellOpen(bar.close - slippage);
            }
        }

        int size = 128;
        visPrice(size, wma, close);

        visProfit(size * 120, profit());

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    private boolean pBar(BarInfo bar, double gap) {
        return (bar.close - bar.open) > Math.abs(gap);
    }

    private boolean nBar(BarInfo bar, double gap) {
        return (bar.open - bar.close) > Math.abs(gap);
    }

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        new AlgorithmManager(Collections.singletonList(new BarAnalysis(pusher))).vis();
    }
}
