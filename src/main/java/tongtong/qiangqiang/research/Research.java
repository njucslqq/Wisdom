package tongtong.qiangqiang.research;


import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies3;

import java.io.File;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import static cn.quanttech.quantera.CONST.INTRA_QUANDIS_URL;
import static cn.quanttech.quantera.CONST.OUTRA_QUANDIS_URL;
import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.H.ticks;
import static tongtong.qiangqiang.func.Util.extract;
import static tongtong.qiangqiang.research.Filter.lowPass;


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

    public static void main(String[] args) {
        setNetDomain(OUTRA_QUANDIS_URL);

        String code = "IF1601";
        LocalDate date = of(2015, 12, 22);
        List<Double> price = extract(ticks(code, date), "lastPrice");
        price.remove(0);
        List<Double> data = new LinkedList<>();
        data.addAll(price.subList(0, 4096));
        data.addAll(price.subList(1000, 5096));

        Transform t = new Transform(new FastWaveletTransform(new Daubechies3()));
        File file = new File(BASE + FILE);
        lowPass(t, data, 0.5, file);
    }
}
