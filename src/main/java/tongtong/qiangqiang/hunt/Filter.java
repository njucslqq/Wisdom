package tongtong.qiangqiang.hunt;

import biz.source_code.dsp.filter.IirFilter;
import jwave.Transform;
import jwave.datatypes.natives.Complex;
import jwave.transforms.AncientEgyptianDecomposition;
import jwave.transforms.DiscreteFourierTransform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.WaveletTransform;
import jwave.transforms.wavelets.haar.Haar1;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.data.FileEcho;
import tongtong.qiangqiang.func.GeneralUtilizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static tongtong.qiangqiang.func.GeneralUtilizer.toComplex;
import static tongtong.qiangqiang.func.GeneralUtilizer.toMagnitude;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-22.
 */
public class Filter {

    public enum WaveType {
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

    public static void warm(List<Double> value, IirFilter iir) {
        for (int i = 0; i < value.size(); i++) {
            double nv = iir.step(value.get(i));
        }
    }

    public static void lowPass(Transform t, List<Double> data, double percent, File file) {
        int size = data.size();
        int top = (int) (size * percent);
        if (t.getBasicTransform() instanceof WaveletTransform) {
            size = 2;
            int exp = 1;
            while (size <= data.size()) {
                size <<= 1;
                exp++;
            }
            size >>= 1;
            exp--;
            top = (int) (exp * percent);
        }
        List<Double> res = lowPassFilter(t, data.subList(0, size), top);
        FileEcho echo = new FileEcho(file.getAbsolutePath());
        echo.writeList(data, res);
        echo.close();
    }

    public static List<Double> lowPassFilter(Transform t, List<Double> data, int top) {
        Complex[] input = toComplex(data);
        Complex[] output = t.forward(input);
        int index = output.length;
        if (t.getBasicTransform() instanceof WaveletTransform) {
            index = 1 << top;
        } else if (t.getBasicTransform() instanceof DiscreteFourierTransform) {
            index = top;
        }
        for (; index < output.length; index++) {
            output[index].setReal(0);
            output[index].setImag(0);
        }
        input = t.reverse(output);
        return toMagnitude(input);
    }

    public static List<Double> highPassFilter(Transform t, List<Double> data, int top) {
        Complex[] input = toComplex(data);
        Complex[] output = t.forward(input);
        int index = output.length;
        if (t.getBasicTransform() instanceof WaveletTransform) {
            index = 1 << top;
        } else if (t.getBasicTransform() instanceof DiscreteFourierTransform) {
            index = top;
        }

        for (int i=0; i < index; i++) {
            output[i].setReal(0);
            output[i].setImag(0);
        }
        input = t.reverse(output);
        return toMagnitude(input);
    }

    public static void filterByAmplitude(List<Double> data, WaveType wave, int number, String file) {
        switch (wave) {
            case FOURIER:
                filterUsingNumber(new Transform(new DiscreteFourierTransform()), data, number, file);
                break;
            case WAVELETS:
                filterUsingNumber(new Transform(new AncientEgyptianDecomposition(new FastWaveletTransform(new Haar1()))), data, number, file);
                break;
        }
    }

    public static void filterUsingNumber(Transform t, final List<Double> data, final int number, final String file) {
        Complex[] input = toComplex(data);
        Complex[] output = t.forward(input);
        Complex[] input1 = t.reverse(output);
        List<Double> reverseData1 = GeneralUtilizer.toReal(input1);

        List<Pair<Complex, Integer>> amplitude = new ArrayList<>();
        for (int i = 0; i < output.length; i++)
            amplitude.add(Pair.of(output[i], i));
        amplitude.sort((a, b) -> ((Double) b.getLeft().getMag()).compareTo(a.getLeft().getMag()));
        for (int i = 0; i < amplitude.size(); i++)
            System.out.println(amplitude.get(i).getLeft().getMag());
        int n = Math.min(number, output.length);
        for (int i = n; i < amplitude.size(); i++) {
            output[amplitude.get(i).getRight()].setReal(0);
            output[amplitude.get(i).getRight()].setImag(0);
        }
        input = t.reverse(output);
        List<Double> reverseData2 = GeneralUtilizer.toReal(input);
        FileEcho echo = new FileEcho(file);
        for (int i = 0; i < data.size() && i < reverseData2.size(); i++)
            echo.writeln(data.get(i), reverseData1.get(i), reverseData2.get(i));
        echo.close();
    }

    public static void filterUsingPercent(Transform t, final List<Double> data, final double percent, final String file) {
        Complex[] input = toComplex(data);
        Complex[] output = t.forward(input);

        double sum = 0.0;
        List<Pair<Complex, Integer>> amplitude = new ArrayList<>();
        for (int i = 0; i < output.length; i++) {
            amplitude.add(Pair.of(output[i], i));
            sum += output[i].getMag();
        }
        double current = 0.0;
        amplitude.sort((a, b) -> ((Double) b.getLeft().getMag()).compareTo(a.getLeft().getMag()));
        int index = 0;
        for (; index < amplitude.size(); index++) {
            System.out.println(amplitude.get(index).getLeft().getMag());
            current += amplitude.get(index).getLeft().getMag();
            if (current >= sum * percent)
                break;
        }
        int n = index + 1;
        for (int i = n; i < amplitude.size(); i++) {
            output[amplitude.get(i).getRight()].setReal(0);
            output[amplitude.get(i).getRight()].setImag(0);
        }
        input = t.reverse(output);
        List<Double> reverseData2 = toMagnitude(input);
        FileEcho echo = new FileEcho(file);
        for (int i = 0; i < data.size() && i < reverseData2.size(); i++)
            echo.writeln(data.get(i), reverseData2.get(i));
        echo.close();
    }
}
