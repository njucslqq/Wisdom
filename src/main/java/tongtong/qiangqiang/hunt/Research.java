package tongtong.qiangqiang.hunt;


import jwave.transforms.wavelets.daubechies.Daubechies3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.CONST.INTRA_QUANDIS_URL;
import static cn.quanttech.quantera.common.data.TimeFrame.MIN_5;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.data.Historical.ticks;
import static tongtong.qiangqiang.func.GeneralUtilizer.*;
import static tongtong.qiangqiang.hunt.Window.win;


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

        String code = "rb1605";
        LocalDate date = of(2015, 6, 1);
        LocalDate end = LocalDate.now().minusDays(3);

        List<Double> barList = extract(bars(code, MIN_5, date, end), "closePrice");
        int NUMA = 7;
        int size = 60;
        WaveChart wcLow = new WaveChart(barList, l -> {
            List<Double> winList = win(barList, size, list -> wma(list, defaultWeights(list.size())));
            List<Double> price = new ArrayList<>();
            for (int i = 0; i < winList.size(); i++) {
                if (i < NUMA)
                    price.add(wma(winList.subList(0, i + 1), defaultWeights(i + 1)));
                else
                    price.add(wma(winList.subList(i + 1 - NUMA, i + 1), defaultWeights(NUMA)));
            }
            return price;
        });

        wcLow.setALL(256).setTAIL(2).setTOP(3);
        wcLow.show(new Daubechies3(), 100);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /*int NUM = 791;
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
        wcHigh.show(new Daubechies3(), 500);*/
    }
}
