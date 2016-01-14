package tongtong.qiangqiang.research;


import jwave.transforms.wavelets.daubechies.Daubechies3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.CONST.INTRA_QUANDIS_URL;
import static cn.quanttech.quantera.common.data.TimeFrame.MIN_1;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.H.bars;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.*;


/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015/11/10.
 */
public class Research {
    public static void main(String[] args) {
        setNetDomain(INTRA_QUANDIS_URL);

        String code = "IF1601";
        LocalDate date = of(2015, 12, 30);
        List<Double> barList = extract(bars(code, MIN_1, date), "closePrice");
        List<Double> tickList = extract(ticks(code, date), "lastPrice");

        int NUMA = 5;
        WaveChart wcLow = new WaveChart(barList, l -> {
            List<Double> price = new ArrayList<>();
            for (int i = 0; i < l.size(); i++) {
                if (i < NUMA)
                    price.add(wma(l.subList(0, i + 1), defaultWeights(i + 1)));
                else
                    price.add(wma(l.subList(i + 1 - NUMA, i + 1), defaultWeights(NUMA)));
            }
            return price;
        });

        wcLow.setALL(256).setTAIL(2).setTOP(3);
        wcLow.show(new Daubechies3(), 500);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int NUM = 791;
        WaveChart wcHigh = new WaveChart(tickList, l -> {
            List<Double> price = new ArrayList<>();
            for (int i = 0; i < l.size(); i++) {
                if (i < NUM)
                    price.add(wma(l.subList(0, i + 1), defaultWeights(i + 1)));
                else
                    price.add(wma(l.subList(i + 1 - NUM, i + 1), defaultWeights(NUM)));
            }
            return price;
        });
        wcHigh.setALL(128).setTAIL(31).setTOP(2);
        wcHigh.show(new Daubechies3(), 500);
    }
}
