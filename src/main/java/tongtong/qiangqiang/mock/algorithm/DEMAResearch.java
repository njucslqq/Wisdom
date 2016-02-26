package tongtong.qiangqiang.mock.algorithm;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.mock.MockBase;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_5;
import static java.time.LocalDate.of;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class DEMAResearch extends MockBase {

    Intermediate close = new Intermediate();

    WMA wma = new WMA(21);

    DEMA dema1_fast = new DEMA(14);

    DEMA dema1 = new DEMA(21);

    DEMA dema2_fast = new DEMA(new WMA(14), new WMA(14));

    DEMA dema2 = new DEMA(new WMA(21), new WMA(21));

    TimeSeriesChart tsc1 = new TimeSeriesChart("DEMA-ema");

    TimeSeriesChart tsc2 = new TimeSeriesChart("DEMA-wma");

    public DEMAResearch(String name) {
        super(name);
    }

    @Override
    public void init() {
        setSecurity("rb1605");
        setResolution(MIN_5);
        setStart(of(2016, 1, 1));
        setEnd(of(2016, 2, 25));
    }

    @Override
    public void onData(BaseData data, int index) {
        BarInfo bar = (BarInfo) data;

        close.update(bar.closePrice);
        double wma1 = wma.update(bar.closePrice);
        double slow1 = dema1.update(bar.closePrice);
        double fast1 = dema1_fast.update(bar.closePrice);
        double slow2 = dema2.update(bar.closePrice);
        double fast2 = dema2_fast.update(bar.closePrice);

        if (wma1 < slow2) {
            buyClose(bar.closePrice);
            buyOpen(bar.closePrice);
        } else {
            sellClose(bar.closePrice);
            sellOpen(bar.closePrice);
        }

        int size = 1280000;
        if (close.dataSize() > size) {
            tsc1.vis("HH:mm:ss", wma.last(size), dema1.last(size), close.last(size));
            tsc2.vis("HH:mm:ss", wma.last(size), dema2.last(size), close.last(size));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
