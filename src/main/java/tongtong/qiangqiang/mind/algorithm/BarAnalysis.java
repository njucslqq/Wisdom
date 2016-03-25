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

    public final WMA wma = new WMA(3);

    public final ArrayList<BarInfo> bars = new ArrayList<>();

    private double slippage = 1.0;

    public BarAnalysis(Pusher pusher) {
        super("BarAnalysis", 0.2, pusher);
    }

    @Override
    public void init() {
        setSecurity("rb1610");
        setResolution(TimeFrame.MIN_5);
        setStart(LocalDate.of(2016, 3, 13));
        setEnd(LocalDate.of(2016, 3, 25));
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

        if (bars.size() > 1){
            BarInfo pre = bars.get(bars.size()-2);
            if (pre.close > pre.open && bar.close > bar.open){
                buyClose(bar.close + slippage);
                buy(bar.close + slippage);
            }
            else if (pre.close < pre.open && bar.close < bar.open){
                sell(bar.close - slippage);
                sellOpen(bar.close - slippage);
            }
        }
        int size = 128;
        visPrice(size, wma, close);

        visProfit(Pair.of("return", profit()));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        new AlgorithmManager(Collections.singletonList(new BarAnalysis(pusher))).vis();
    }
}
