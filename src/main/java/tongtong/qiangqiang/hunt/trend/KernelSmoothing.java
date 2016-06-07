package tongtong.qiangqiang.hunt.trend;

import cn.quanttech.quantera.common.datacenter.HistoricalData;
import cn.quanttech.quantera.common.datacenter.source.QuandisSource;
import cn.quanttech.quantera.common.type.quotation.BarInfo;
import org.apache.commons.lang3.ArrayUtils;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.quanttech.quantera.common.datacenter.HistoricalData.bars;
import static cn.quanttech.quantera.common.type.data.TimeFrame.MIN_1;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-06-03.
 */
public class KernelSmoothing {

    public static Double[] GaussianKernelSmoothing(Double[] value, int radius) {
        int n = value.length;
        Double[] res = new Double[n];
        double p = 2.0 * radius * radius;
        for (int i = 0; i < n; i++) {
            int l = Math.max(0, i - radius);
            int u = Math.min(n - 1, i + radius);
            double KSum = 0.0, ValueSum = 0.0;
            for (int j = l; j <= u; j++) {
                double k = Math.exp(-Math.pow(j - i, 2) / p);
                KSum += k;
                ValueSum += k * value[j];
            }
            res[i] = ValueSum / KSum;
        }
        return res;
    }

    public static void main(String[] args) {
        QuandisSource.def = new QuandisSource(QuandisSource.OUTRA);
        LocalDate start = LocalDate.of(2016, 5, 11);
        LocalDate end = LocalDate.of(2016, 5, 18);
        List<BarInfo> bars = HistoricalData.bars("rb1610", MIN_1, start, end);
        Double[] value = new Double[bars.size()];
        for (int i = 0; i < bars.size(); i++)
            value[i] = bars.get(i).close;
        Double[] smooth = GaussianKernelSmoothing(value, 37);
        TimeSeriesChart chart = new TimeSeriesChart("Kernel Smoothing");
        chart.vis("HH:mm:ss", Arrays.asList(value), Arrays.asList(smooth));
    }
}
