package tongtong.qiangqiang.research;


import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import jwave.Transform;
import jwave.datatypes.natives.Complex;
import jwave.transforms.DiscreteFourierTransform;
import tongtong.qiangqiang.func.Util;
import tongtong.qiangqiang.func.WindowHandler;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import static biz.source_code.dsp.filter.FilterCharacteristicsType.butterworth;
import static biz.source_code.dsp.filter.FilterPassType.highpass;
import static biz.source_code.dsp.filter.FilterPassType.lowpass;
import static biz.source_code.dsp.filter.IirFilterDesignFisher.design;
import static cn.quanttech.quantera.CONST.INTRA_QUANDIS_URL;
import static java.time.LocalDate.of;
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

    public static final String BASE = "./signal/";

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(INTRA_QUANDIS_URL);

        String code = "IF1601";
        LocalDate warmDay = of(2015, 12, 14);
        LocalDate targetDay = of(2015, 12, 22);
        //List<Double> warmData = extract(ticks(code, warmDay), "lastPrice");
        List<Double> targetData = extract(ticks(code, targetDay), "lastPrice");//.subList(0, 1231);


        //int size = 13;
        //targetData = Util.window(targetData, size);

        //int number = 3;
        double percent = 0.1;
        String file = BASE + code + "[" + targetData.size() + "]" + targetDay.toString() + "FilterbyAmplitude[" + percent + "].csv";
        Filter.fourierPercent(targetData, percent, file);



        /*
        //wave
        String file = BASE + "jwave-" + code + "-" + targetDay.toString() + "bfore.csv";
        wave(targetData, file);

        int size = 29;
        warmData = Util.window(warmData, size);
        targetData = Util.window(targetData, size);

        //wave
        String file2 = BASE + "jwave-" + code + "-" + targetDay.toString() + "after.csv";
        wave(targetData, file2);

        /*double bond = 0.09;
        String base = BASE + code + "-" + targetDay.toString() + "-" + size + "-";
        lowPassFilter(warmData, targetData, bond, base);
        */
    }


    public static void wave(List<Double> data, String file) {
        int size = 2;
        while (size <= data.size())
            size <<= 1;
        size >>= 1;
        Complex[] time = Util.toComplex(data.subList(0, size));
        Transform transform = new Transform(new DiscreteFourierTransform());
        Complex[] frequency = transform.forward(time);
        FileEcho echo = new FileEcho(file);
        for (int i = 0; i < time.length; i++)
            if (i < 200)
                echo.writeln(time[i].getReal(), frequency[i].getMag());
            else
                echo.writeln(time[i].getReal());
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
        List<Double> warm = extract(ticks(code, of(2015, 12, 1), of(2015, 12, 7)), "lastPrice");
        Filter.warm(warm, iir);
        LocalDate day = of(2015, 12, 8);
        for (int i = 0; i < 3; i++) {
            LocalDate thisDay = day.plusDays(i);
            String file = BASE + code + "-" + thisDay.toString() + "-" + identity(pass, algo, order, ripple, fcf1, fcf2) + ".csv";
            List<Double> price = extract(ticks(code, thisDay), "lastPrice");
            if (!price.isEmpty())
                Filter.filter(file, price, iir, 500);
        }
    }

    private static void lowPassFilter(List<Double> warmData, List<Double> targetData, double threshold, String base) {
        FilterPassType pass = lowpass;
        FilterCharacteristicsType algo = butterworth;
        int order = 2;
        double ripple = 2;
        double fcf2 = 0.4;

        IirFilterCoefficients coe = design(pass, algo, order, ripple, threshold, fcf2);
        System.out.println("阶数: " + coe.a.length + "," + coe.b.length);
        IirFilter iir = new IirFilter(coe);
        Filter.warm(warmData, iir);

        String file = base + identity(pass, algo, order, ripple, threshold, fcf2) + ".csv";
        Filter.filter(file, targetData, iir, 0);
    }

    public static IirFilter getLowPassFilter(FilterCharacteristicsType algo, double threshold) {
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
