package tongtong.qiangqiang.mock.algorithm;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.mock.MockBase;

import java.time.LocalDate;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class MovingAverageDifference extends MockBase {

    public final MovingAverage fast;

    public final MovingAverage slow;

    public final TimeFrame resolution;

    public MovingAverageDifference(MovingAverage fast, MovingAverage slow, TimeFrame resolution) {
        super(fast.getClass().getSimpleName() + " - " + slow.getClass().getSimpleName());
        System.out.println(fast.getClass().getSimpleName() + " - " + slow.getClass().getSimpleName());
        this.fast = fast;
        this.slow = slow;
        this.resolution = resolution;
    }

    @Override
    public void init() {
        setSecurity("rb1605");
        setResolution(resolution);
        setStart(LocalDate.of(2016, 2, 1));
        setEnd(LocalDate.now());
    }

    @Override
    public void onData(BaseData data, int index) {
        BarInfo bar = (BarInfo) data;
        double price = bar.closePrice;

        double f = fast.update(price);
        double s = slow.update(price);

        if (f > s) {
            buyClose(price);
            buyOpen(price);
        } else {
            sellClose(price);
            sellOpen(price);
        }
    }
}
