package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.MAVGFactory;
import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.composite.MACD;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static cn.quanttech.quantera.common.data.TimeFrame.MIN_5;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static org.apache.commons.lang3.tuple.Pair.of;
import static tongtong.qiangqiang.data.factor.MAVGFactory.create;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-03.
 */
public class PriceChange extends Algorithm {

    public final MACD macd;

    public final Intermediate close = new Intermediate();

    public final TimeFrame resolution;

    public final String security;

    public final LocalDate begin;

    public final int share = 1;

    public final double slipage = 0.0;

    public final MAVG fast_dif;

    public final MAVG slow_dif;

    public final Intermediate dif_dif;

    public PriceChange(String name, Pusher trader, MACD macd, String security, TimeFrame resolution, LocalDate begin) {
        super(name, trader);
        this.macd = macd;
        this.resolution = resolution;
        this.security = security;
        this.begin = begin;
        fast_dif = create(macd.fast.getClass(), macd.fast.getPeriod());
        slow_dif = create(macd.slow.getClass(), macd.slow.getPeriod());
        dif_dif = new Intermediate();
    }

    @Override
    public void init() {
        setSecurity(security);
        setResolution(resolution);
        setStart(begin);
        setEnd(LocalDate.now());
    }

    @Override
    public void onData(BaseData data) {
        BarInfo bar = (BarInfo) data;
        double price = bar.closePrice;

        close.update(price);
        macd.update(price);

        double fd = fast_dif.update(macd.dif.value.tail());
        double sd = slow_dif.update(macd.dif.value.tail());
        dif_dif.update(fd - sd);

        int size = 128;
        if (close.size() < size) {
            visPrice(of(macd.fast.getName(), macd.fast.all()), of(macd.slow.getName(), macd.slow.all()), of("close", close.all()));
            //visProfit(of("DIF_DIF", dif_dif.all()));
        } else {
            visPrice(of(macd.fast.getName(), macd.fast.lastn(size)), of(macd.slow.getName(), macd.slow.lastn(size)), of("close", close.lastn(size)));
            //visProfit(of("DIF_DIF", dif_dif.lastn(size)));
        }

        if (dif_dif.size() > 1) {
            if (macd.dif.value.last(0) < macd.dif.value.last(1)) {
                buyClose(security, share, price + slipage);
                buy(security, share, price + slipage);
            } else {
                sell(security, share, price - slipage);
                sellOpen(security, share, price - slipage);
            }
        }

        visProfit(of("return", profit()));
    }

    public static void main(String[] args) {
        setNetDomain(CONST.OUTRA_QUANDIS_URL);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        int period = 25;
        String security = "rb1605";
        TimeFrame resolution = MIN_1;
        LocalDate begin = LocalDate.of(2016, 2, 1);

        Class<?>[] clazz = {SMA.class, EMA.class, WMA.class, DEMA.class};
        List<Algorithm> algorithms = new ArrayList<>();
        for (int i = 1; i < clazz.length; i++)
            for (int j = 0; j < i; j++) {
                MAVG fast = create(clazz[i], period);
                MAVG slow = create(clazz[j], period);
                MACD macd = new MACD(fast, slow, new SMA(5));
                algorithms.add(new PriceChange(fast.getName() + "-" + slow.getName(), pusher, macd, security, resolution, begin));
            }

        new AlgorithmManager(algorithms).vis();
    }
}
