package tongtong.qiangqiang.mind;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.TimeFrame;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.factor.MAVG;
import tongtong.qiangqiang.data.factor.composite.DEMA;
import tongtong.qiangqiang.data.factor.single.indicators.EMA;
import tongtong.qiangqiang.data.factor.single.indicators.SMA;
import tongtong.qiangqiang.data.factor.single.indicators.WMA;
import tongtong.qiangqiang.mind.algorithm.MAVGReverseDiff;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static java.time.LocalDate.of;
import static org.apache.commons.lang3.tuple.Pair.of;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class AlgorithmDriver {

    public static void main(String[] args) {
        setNetDomain(CONST.INTRA_QUANDIS_URL);

        Pusher pusher = new Pusher(8080);
        pusher.run();

        String security = "rb1605";
        TimeFrame resolution = MIN_1;
        LocalDate begin = of(2016, 2, 22);

        List<Pair<String, Double>> results = new ArrayList<>();
        List<Algorithm> algorithms = new ArrayList<>();

        int period = 17;
        MAVG[] mavgs = {new SMA(period), new EMA(period), new WMA(period), new DEMA(period), new DEMA(new WMA(period), new WMA(period))};

        /*for (int i = 1; i < mavgs.length; i++)
            for (int j = 0; j < i; j++) {
                MAVG[] fast = {new SMA(period), new EMA(period), new WMA(period), new DEMA(period), new DEMA(new WMA(period), new WMA(period))};
                MAVG[] slow = {new SMA(period), new EMA(period), new WMA(period), new DEMA(period), new DEMA(new WMA(period), new WMA(period))};
                algorithms.add(new MAVGReverseDiff("[" + i + "," + j + "]", pusher, security, resolution, begin, fast[i], slow[j]));
            }
        */
        algorithms.add(new MAVGReverseDiff("", pusher, security, resolution, begin, new DEMA(period), new EMA(period)));// fast[i], slow[j]));
        algorithms.parallelStream().forEach(algorithm -> {
            algorithm.run();
            synchronized (results) {
                results.add(of(algorithm.getName(), algorithm.total()));
            }
        });

        List<Pair<String, Double>> res = results.stream().sorted((a, b) -> b.getRight().compareTo(a.getRight())).collect(Collectors.toList());
        for (Pair<String, Double> p : res)
            System.out.println(p.getLeft() + " , " + p.getRight());
    }
}
