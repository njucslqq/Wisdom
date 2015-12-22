package tongtong.qiangqiang.research;

import biz.source_code.dsp.filter.IirFilter;

import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-22.
 */
public class Filter {

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
}
