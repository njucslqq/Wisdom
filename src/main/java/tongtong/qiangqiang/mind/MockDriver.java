package tongtong.qiangqiang.mind;

import cn.quanttech.quantera.CONST;
import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.mind.algorithm.MovingAverageDifference;

import java.time.LocalDate;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_5;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockDriver {

    public static void main(String[] args) {
        setNetDomain(CONST.OUTRA_QUANDIS_URL);

        String security = "rb1605";
        int share = 1;
        LocalDate begin = LocalDate.of(2016, 1, 1);

        int period = 25;
        MovingAverage[] mavgs = {new SMA(period), new EMA(period), new WMA(period), new DEMA(period), new DEMA(new WMA(period), new WMA(period))};

        for (int i = 1; i < mavgs.length; i++)
            for (int j = 0; j < i; j++) {
                MovingAverage[] mavgs_fast = {new SMA(period), new EMA(period), new WMA(period), new DEMA(period), new DEMA(new WMA(period), new WMA(period))};
                MovingAverage[] mavgs_slow = {new SMA(period), new EMA(period), new WMA(period), new DEMA(period), new DEMA(new WMA(period), new WMA(period))};
                MovingAverageDifference algorithm = new MovingAverageDifference(mavgs_fast[i], mavgs_slow[j], MIN_5, security, share, begin, mavgs_fast[i].getClass().getSimpleName() + " - " + mavgs_slow[j].getClass().getSimpleName());
                algorithm.run();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }
}
