package tongtong.qiangqiang.research;


import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies3;
import jwave.transforms.wavelets.haar.Haar1;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cn.quanttech.quantera.CONST.INTRA_QUANDIS_URL;
import static cn.quanttech.quantera.CONST.OUTRA_QUANDIS_URL;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.extract;
import static tongtong.qiangqiang.research.Filter.lowPass;
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

    public static final int ALL = 2048;

    public static final int TAIL = 100;

    public static final int HEAD = ALL - TAIL;

    public static final int TOP = 4;

    public static void main(String[] args) {
        setNetDomain(INTRA_QUANDIS_URL);

        int sample = 10;
        String code = "IF1601";
        LocalDate date = of(2015, 12, 22);
        List<Double> price = extract(ticks(code, date), "lastPrice");
        price.remove(0);
        int span = price.size() / sample;

        int index = 50;
        for (; index < price.size(); index += span) {
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
            File file = new File(BASE + index + "[" + TOP + "]" + FILE);
            FileEcho echo = new FileEcho(file.getAbsolutePath());
            int i = HEAD > (index + 1) ? HEAD - index - 1 : 0;
            for (; i < HEAD; i++)
                echo.writeln(data.get(i), res.get(i));
            echo.close();
        }
    }
}
