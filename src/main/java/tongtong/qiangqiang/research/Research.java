package tongtong.qiangqiang.research;


import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;

import java.time.LocalDate;
import java.util.List;

import static biz.source_code.dsp.filter.FilterCharacteristicsType.butterworth;
import static biz.source_code.dsp.filter.FilterPassType.lowpass;
import static biz.source_code.dsp.filter.IirFilterDesignFisher.design;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.extract;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015/11/10.
 */
public class Research {

    private static final String BASE = "D:\\investment\\signal\\single\\";

    public static void main(String[] args) {
        FilterPassType pass = lowpass;
        FilterCharacteristicsType algo = butterworth;
        int order = 3;
        double ripple = 2;
        double fcf1 = 0.0005;
        double fcf2 = 0.4;
        IirFilterCoefficients coe = design(pass, algo, order, ripple, fcf1, fcf2);
        IirFilter iir = new IirFilter(coe);

        String code = "IF1512";
        List<Double> warm = extract(ticks(code, LocalDate.of(2015, 12, 1), LocalDate.of(2015, 12, 7)), "lastPrice");
        Filter.warm(warm, iir);
        LocalDate day = LocalDate.of(2015, 12, 8);
        for(int i=0; i<15; i++ ) {
            LocalDate thisDay = day.plusDays(i);
            String file = BASE + code+"-"+thisDay.toString()+"-"+ identity(pass, algo, order, ripple, fcf1, fcf2) + ".csv";
            List<Double> price = extract(ticks(code, thisDay), "lastPrice");
            if (!price.isEmpty())
                Filter.filter(file, price, iir, 0);
        }
    }

    private static String identity(FilterPassType pass, FilterCharacteristicsType algo, int order, double ripple, double fcf1, double fcf2){
        return "filter-" + algo.toString() + "-" + pass.toString() + "-" + order + "-" + ripple + "-" + fcf1 + "-" + fcf2;
    }
}
