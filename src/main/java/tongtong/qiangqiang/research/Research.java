package tongtong.qiangqiang.research;


import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import cn.quanttech.quantera.common.data.TickInfo;
import tongtong.qiangqiang.func.Util;
import tongtong.qiangqiang.func.WindowHandler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static biz.source_code.dsp.filter.FilterCharacteristicsType.butterworth;
import static biz.source_code.dsp.filter.FilterPassType.bandpass;
import static biz.source_code.dsp.filter.FilterPassType.highpass;
import static biz.source_code.dsp.filter.FilterPassType.lowpass;
import static biz.source_code.dsp.filter.IirFilterDesignFisher.design;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.defaultWeights;
import static tongtong.qiangqiang.func.Util.extract;
import static tongtong.qiangqiang.func.Util.wma;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015/11/10.
 */
public class Research {

    public static final String BASE = "D:\\investment\\signal\\single\\";

    public static void main(String[] args) {
        int size = 13;
        String code = "IF1512";
        LocalDate day = LocalDate.of(2015, 11, 27);
        List<Double> win = window(code, day, size);

        LocalDate warDay = LocalDate.of(2015, 11, 26);
        List<Double> warm = extract(ticks(code, warDay), "lastPrice");
        double bond = 0.1;
        lowPassFilter(warm, win, bond);
    }

    private static String identity(FilterPassType pass, FilterCharacteristicsType algo, int order, double ripple, double fcf1, double fcf2) {
        return "filter-" + algo.toString() + "-" + pass.toString() + "-" + order + "-" + ripple + "-" + fcf1 + "-" + fcf2;
    }

    private static void filter() {
        FilterPassType pass = highpass;
        FilterCharacteristicsType algo = butterworth;
        int order = 3;
        double ripple = 2;
        double fcf1 = 0.01;
        double fcf2 = 0.02;
        IirFilterCoefficients coe = design(pass, algo, order, ripple, fcf1, fcf2);
        IirFilter iir = new IirFilter(coe);

        String code = "IF1512";
        List<Double> warm = extract(ticks(code, LocalDate.of(2015, 12, 1), LocalDate.of(2015, 12, 7)), "lastPrice");
        Filter.warm(warm, iir);
        LocalDate day = LocalDate.of(2015, 12, 8);
        for (int i = 0; i < 3; i++) {
            LocalDate thisDay = day.plusDays(i);
            String file = BASE + code + "-" + thisDay.toString() + "-" + identity(pass, algo, order, ripple, fcf1, fcf2) + ".csv";
            List<Double> price = extract(ticks(code, thisDay), "lastPrice");
            if (!price.isEmpty())
                Filter.filter(file, price, iir, 500);
        }
    }

    private static void lowPassFilter(List<Double> warm, List<Double> value, double threshold) {
        FilterPassType pass = lowpass;
        FilterCharacteristicsType algo = butterworth;
        int order = 2;
        double ripple = 2;
        double fcf2 = 0.4;

        IirFilterCoefficients coe = design(pass, algo, order, ripple, threshold, fcf2);
        System.out.println("阶数: " + coe.a.length + "," + coe.b.length);
        IirFilter iir = new IirFilter(coe);
        Filter.warm(warm, iir);

        String file = BASE + identity(pass, algo, order, ripple, threshold, fcf2) + ".csv";
        Filter.filter(file, value, iir, 0);
    }

    public static IirFilter getLowPassFilter(FilterCharacteristicsType algo, double threshold){
        FilterPassType pass = lowpass;
        int order = 2;
        double ripple = 2;
        double fcf2 = 0.4;

        IirFilterCoefficients coe = design(pass, algo, order, ripple, threshold, fcf2);
        System.out.println("阶数: " + coe.a.length + "," + coe.b.length);
        return new IirFilter(coe);
    }

    private static List<Double> window(String code, LocalDate day, int size) {
        final List<Double> res = new LinkedList<>();
        List<TickInfo> ticks = ticks(code, day);
        ticks.remove(0);
        List<Double> warm = extract(ticks, "lastPrice");
        String file = BASE + "Window-" + code + "-" + day.toString() + "-" + size + ".csv";
        Window.win(file, warm, size, new WindowHandler() {
            @Override
            public double handle(List<Double> data) {
                if (data.isEmpty())
                    return 0.0;
                double avg = wma(data, defaultWeights(size));  //data.stream().mapToDouble(d -> d).sum() / data.size();
                res.add(avg);
                return avg;
            }
        });
        return res;
    }
}
