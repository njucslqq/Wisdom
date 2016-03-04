package tongtong.qiangqiang.mind.algorithm;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.composite.MACD;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.Intermediate;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.mind.Algorithm;
import tongtong.qiangqiang.mind.app.AlgorithmManager;
import tongtong.qiangqiang.mind.trade.Pusher;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static cn.quanttech.quantera.common.data.TimeFrame.MIN_5;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static org.apache.commons.lang3.tuple.Pair.of;

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

    public final Intermediate tprice = new Intermediate();

    public final TimeFrame resolution;

    public final String security;

    public final LocalDate begin;

    public final int share = 1;

    public final double slipage = 0.0;

    public PriceChange(String name, Pusher trader, MACD macd, String security, TimeFrame resolution, LocalDate begin) {
        super(name, trader);
        this.macd = macd;
        this.resolution = resolution;
        this.security = security;
        this.begin = begin;
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

        double tp = (bar.highPrice + bar.lowPrice + bar.closePrice) / 3.0;
        double price = bar.closePrice;
        close.update(bar.closePrice);
        tprice.update(tp);
        macd.update(price);

        int size = 128;
        if (close.size() < size) {
            visPrice(of(macd.fast.getName(), macd.fast.all()), of(macd.slow.getName(), macd.slow.all()), of("close", close.all()));
            //visProfit(of("dif", macd.dea.all()));
        } else {
            visPrice(of(macd.fast.getName(), macd.fast.lastn(size)), of(macd.slow.getName(), macd.slow.lastn(size)), of("close", close.lastn(size)));
            //visProfit(of("dif", macd.dea.lastn(size)));
        }

        SMA dea = (SMA) macd.dea;
        if (dea.size() >= 2) {
            if (dea.value.tail() > dea.value.last(1)){
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
        setNetDomain(CONST.INTRA_QUANDIS_URL);

        //Pusher pusher = new Pusher(8080);
        //pusher.run();

        int period = 31;
        List<Algorithm> algorithms = new ArrayList<>();

        String security = "rb1605";
        TimeFrame resolution = MIN_5;
        LocalDate begin = LocalDate.of(2016, 1, 1);

        MACD macd = new MACD(new EMA(period), new SMA(period), new SMA(13));
        algorithms.add(new PriceChange("Difference Change", null, macd, security, resolution, begin));

        JFrame frame = new JFrame("AlgorithmManager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new AlgorithmManager(algorithms));
        frame.pack();
        frame.setVisible(true);
    }
}
