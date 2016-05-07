package tongtong.qiangqiang.hunt.rnn.util;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-05-06.
 */
public class MathUtil {

    public static double linearNorm(double x, double max, double min) {
        return -1.0 + 2.0 * (x - min) / (max - min);
    }

    public static double linearDenorm(double x, double max, double min) {
        return min + (max - min) * (x + 1) / 2.0;
    }

    public static double logNorm(double x) {
        double v = Math.log10(1 + Math.abs(x));
        if (x < 0)
            return -v;
        return v;
    }

    public static double logDenorm(double x) {
        double v = Math.pow(10.0, Math.abs(x)) -1;
        if (x < 0)
            return -v;
        return v;
    }
}
