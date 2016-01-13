package tongtong.qiangqiang.research;


import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.common.data.TimeFrame;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies3;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import tongtong.qiangqiang.data.H;
import tongtong.qiangqiang.vis.TimeSeriesChart;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static cn.quanttech.quantera.CONST.INTRA_QUANDIS_URL;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.func.Util.extract;
import static tongtong.qiangqiang.research.Filter.lowPassFilter;


/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015/11/10.
 */
public class Research {

    public static final String BASE = "./signal/";

    public static final String FILE = "lowpass.csv";

    public static final int ALL = 1024;

    public static final int TAIL = 37;

    public static final int HEAD = ALL - TAIL;

    public static final int TOP = 3;

    public static final String FMT = "HH-mm-ss.S";

    public static void main(String[] args) {
        setNetDomain(INTRA_QUANDIS_URL);

        TimeSeriesChart tscB = new TimeSeriesChart("Time Series");
        TimeSeriesChart tscA = new TimeSeriesChart("Time Series");

        int sample = 10;
        String code = "IF1601";
        LocalDate date = of(2015, 12, 22);
        List<TickInfo> tickList = H.ticks(code, date); //H.bars(code, TimeFrame.MIN_1, date.minusDays(20), date.plusDays(30));
        tickList.remove(0);
        List<Double> price = extract(tickList, "lastPrice");

        int index = 1024;
        for (; index < price.size(); index++) {
            List<Double> data = new ArrayList<>();
            if (index + 1 < HEAD) {
                int count = HEAD - (index + 1);
                data.addAll(price.subList(0, count));
                data.addAll(price.subList(0, index + 1));
            } else
                data.addAll(price.subList(index + 1 - HEAD, index + 1));
            for (int j = 0; j < TAIL; j++)
                data.add(price.get(index));

            Transform t = new Transform(new FastWaveletTransform(new Daubechies3()));
            List<Double> res = lowPassFilter(t, data, TOP);

            TimeSeries before = new TimeSeries("before");
            TimeSeries after = new TimeSeries("after");
            int i = HEAD > (index + 1) ? 0 : index + 1 - HEAD;
            int j = HEAD > (index + 1) ? HEAD - index - 1 : 0;
            for (; i <= index; i++,j++) {
                before.addOrUpdate(new Millisecond(Date.from(tickList.get(i).tradingTime.atZone(ZoneId.systemDefault()).toInstant())), price.get(i));
                after.addOrUpdate(new Millisecond(Date.from(tickList.get(i).tradingTime.atZone(ZoneId.systemDefault()).toInstant())), res.get(j));
            }

            tscB.vis(FMT, before);
            tscA.vis(FMT, after);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
