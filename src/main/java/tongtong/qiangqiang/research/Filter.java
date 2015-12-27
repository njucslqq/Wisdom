package tongtong.qiangqiang.research;

import biz.source_code.dsp.filter.IirFilter;
import jwave.datatypes.natives.Complex;
import jwave.transforms.DiscreteFourierTransform;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.func.Util;

import java.util.ArrayList;
import java.util.List;

import static tongtong.qiangqiang.func.Util.toComplex;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-22.
 */
public class Filter {

    public enum WaveType{
        FOURIER, WAVELETS
    }

    public static void filter(String file, List<Double> value, IirFilter iir, int omit) {
        FileEcho echo = new FileEcho(file);
        echo.writeln("value", "filter");
        for (int i = 0; i < value.size(); i++) {
            double nv = iir.step(value.get(i));
            if (i >= omit)
                echo.writeln(value.get(i), nv);
        }
    }

    public static void warm(List<Double> value, IirFilter iir){
        for (int i = 0; i < value.size(); i++) {
            double nv = iir.step(value.get(i));
        }
    }

    public static void filterByAmplitude(List<Double> data, WaveType wave, int number, String file){
        switch (wave){
            case FOURIER:
                fourier(data, number, file);
                break;
        }
    }

    public static void fourier(final List<Double> data, final int number, final String file){
        DiscreteFourierTransform fourier = new DiscreteFourierTransform();
        Complex[] input = toComplex(data);
        Complex[] output = fourier.forward(input);
        Complex[] input1 = fourier.reverse(output);
        List<Double> reverseData1 = Util.toReal(input1);

        List<Pair<Complex, Integer>> amplitude = new ArrayList<>();
        for (int i=0;i<output.length;i++)
            amplitude.add(Pair.of(output[i], i));
        amplitude.sort((a, b) -> ((Double)b.getLeft().getMag()).compareTo(a.getLeft().getMag()));
        for (int i=0; i<amplitude.size();i++)
            System.out.println(amplitude.get(i).getLeft().getMag());
        int n = Math.min(number, output.length);
        for (int i=n;i<amplitude.size();i++) {
            output[amplitude.get(i).getRight()].setReal(0);
            output[amplitude.get(i).getRight()].setImag(0);
        }
        input = fourier.reverse(output);
        List<Double> reverseData2 = Util.toReal(input);
        FileEcho echo = new FileEcho(file);
        for (int i=0; i<data.size() && i<reverseData2.size();i++)
            echo.writeln(data.get(i), reverseData1.get(i), reverseData2.get(i));
        echo.close();
    }
}
