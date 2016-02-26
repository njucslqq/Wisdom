package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MovingAverage;
import tongtong.qiangqiang.mind.MockBase;

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

    public final String security;

    public final int share;

    public final LocalDate begin;

    public MovingAverageDifference(MovingAverage fast, MovingAverage slow, TimeFrame resolution, String security, int share, LocalDate begin, String name) {
        super(name);
        this.security = security;
        this.share = share;
        this.begin = begin;
        this.fast = fast;
        this.slow = slow;
        this.resolution = resolution;
        System.out.println(name);
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(begin);
        setEnd(LocalDate.now());
        //setModel(Model.TRADE);
        //setType(OrderType.MARKET);
        //setState(State.MOCK);
    }

    @Override
    public void onData(BaseData data, int index) {
        BarInfo bar = (BarInfo) data;
        double price = bar.closePrice;

        double f = fast.update(price);
        double s = slow.update(price);

        if (f > s) {
            order.buyClose(security, share, price);
            order.buy(security, share, price);
        } else {
            order.sell(security, share, price);
            order.sellOpen(security, share, price);
        }
    }
}
