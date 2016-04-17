package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.factor.single.indicators.RAW;
import cn.quanttech.quantera.common.factor.single.indicators.WMA;
import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.BaseData;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static cn.quanttech.quantera.common.type.data.TimeFrame.MIN_10;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-22.
 */
public class BarAnalysis extends Algorithm {

    public final ArrayList<BarInfo> bars = new ArrayList<>();

    public final RAW close = new RAW();

    public final double gap;

    public final String security;

    public final TimeFrame resolution;

    public BarAnalysis(Pusher pusher, double gap, String security, TimeFrame resolution) {
        super("BarAnalysis", 0.2, pusher);
        this.gap = gap;
        this.security = security;
        this.resolution = resolution;
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(LocalDate.of(2016, 4, 1));
        setEnd(LocalDate.of(2016, 4, 30));

        //setModel(MindType.Model.TRADE);
        //setState(MindType.State.REAL);
    }

    @Override
    public void onData(BaseData data) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BarInfo bar = (BarInfo) data;
        bars.add(0, bar);
        close.update(bar.close);

        if (bars.size() > 1) {
            if (pBar(bars.get(1), gap) && pBar(bars.get(0), gap)) {
                beep(3, 1000);
            } else if (nBar(bars.get(1), gap) && nBar(bars.get(0), gap)) {
                beep(9, 250);
            }
        }

        int size = 128;
        visPrice(size, close);
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

        new AlgorithmManager(Collections.singletonList(new BarAnalysis(pusher, 0.9, "rb1610", MIN_10))).vis();
    }
}
