package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.CONST;
import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TickInfo;
import cn.quanttech.quantera.datacenter.DataCenterUtil;
import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.daubechies.Daubechies3;
import tongtong.qiangqiang.func.GeneralUtilizer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cn.quanttech.quantera.common.data.TimeFrame.TICK;
import static java.time.LocalDate.of;
import static tongtong.qiangqiang.data.Historical.ticks;
import static tongtong.qiangqiang.func.GeneralUtilizer.*;
import static tongtong.qiangqiang.hunt.Filter.lowPassFilter;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockDriver extends MockBase {

    public static final String BASE = "./signal/";

    public static final String FILE = "trade.csv";

    public static final int ALL = 2048;

    public static final int TAIL = 61;

    public static final int HEAD = ALL - TAIL;

    public static final int TOP = 4;

    int span = 50;

    int delay = 15;

    String code = "IF1601";

    LocalDate ext = of(2015, 12, 22);

    List<Double> extra = extract(ticks(code, ext), "lastPrice");

    List<Double> price = new LinkedList<>();

    @Override
    void init() {
        setSecurity(code);
        setResolution(TICK);
        setStart(of(2015, 12, 21));
        setEnd(of(2015, 12, 21));
    }

    @Override
    void onData(BaseData dataUnit, int index) {
        TickInfo tick = (TickInfo) dataUnit;
        price.add(tick.lastPrice);



        if (index + 2 < delay )
            return;

        List<Double> data = new ArrayList<>();
        if (index + 1 < HEAD) {
            int count = HEAD - (index + 1);
            data.addAll(extra.subList(0, count));
            data.addAll(price.subList(0, index + 1));
        } else
            data.addAll(price.subList(index + 1 - HEAD, index + 1));
        for (int j = 0; j < TAIL; j++)
            data.add(price.get(index));

        Transform t = new Transform(new FastWaveletTransform(new Daubechies3()));
        List<Double> res = lowPassFilter(t, data, TOP);


        int nfast = 17;
        int nslow = 23;
        double fast = GeneralUtilizer.wma(res.subList(HEAD - nfast, HEAD), GeneralUtilizer.defaultWeights(nfast));
        double slow = GeneralUtilizer.wma(res.subList(HEAD - nslow, HEAD), GeneralUtilizer.defaultWeights(nslow));


        if(fast > slow) {
            boolean f = buyOpen(tick.lastPrice);
            buyClose(tick.lastPrice);
            if (f) {
                /*if(longTime % span ==0) {
                    File file = new File(BASE + index + "[开仓" + TOP + "]" + FILE);
                    FileEcho echo = new FileEcho(file.getAbsolutePath());
                    int i = HEAD > (index + 1) ? HEAD - index - 1 : 0;
                    for (; i < HEAD; i++)
                        echo.writeln(data.get(i), res.get(i));
                    echo.close();
                }*/

                //price.remove(index);
                //price.add(tick.lastPrice + 15);
            }
        }
        else if(fast < slow){
            sellOpen(tick.lastPrice);
            boolean f = sellClose(tick.lastPrice);
            if (f){
                /*if(longTime % span ==0) {
                    File file = new File(BASE + index + "[平仓" + TOP + "]" + FILE);
                    FileEcho echo = new FileEcho(file.getAbsolutePath());
                    int i = HEAD > (index + 1) ? HEAD - index - 1 : 0;
                    for (; i < HEAD; i++)
                        echo.writeln(data.get(i), res.get(i));
                    echo.close();
                }*/

                //price.remove(index);
                //price.add(tick.lastPrice + 30);
            }
        }
    }

    @Override
    void onComplete() {
        System.out.println("多头盈利：" + longDiff);
        System.out.println("空头盈利：" + shortDiff);
        //FileEcho echo = new FileEcho(BASE + FILE);
        /*for (Double d : price)
            echo.writeln(d);
        echo.close();*/
    }

    public static void main(String[] args) {
        DataCenterUtil.setNetDomain(CONST.INTRA_QUANDIS_URL);
        MockDriver m = new MockDriver();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
