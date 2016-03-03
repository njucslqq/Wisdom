package tongtong.qiangqiang.mind;

import cn.quanttech.quantera.common.data.BaseData;
import cn.quanttech.quantera.common.data.TimeFrame;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartPanel;
import tongtong.qiangqiang.mind.MindType.Model;
import tongtong.qiangqiang.mind.app.AlgorithmPanel;
import tongtong.qiangqiang.mind.order.IOrder;
import tongtong.qiangqiang.mind.order.MockOrder;
import tongtong.qiangqiang.mind.order.RealOrder;
import tongtong.qiangqiang.mind.trade.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static tongtong.qiangqiang.data.Historical.bars;
import static tongtong.qiangqiang.data.Historical.ticks;
import static tongtong.qiangqiang.mind.MindType.Model.TEST;
import static tongtong.qiangqiang.mind.MindType.Order;
import static tongtong.qiangqiang.mind.MindType.Order.MARKET;
import static tongtong.qiangqiang.mind.MindType.State;
import static tongtong.qiangqiang.mind.MindType.State.MOCK;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public abstract class Algorithm {

    private String security;

    private TimeFrame resolution;

    private LocalDate start;

    private LocalDate end;

    private Model model = TEST;

    private State state = MOCK;

    private Order type = MARKET;

    private boolean print = false;

    private final String name;

    private final Pusher trader;

    private final String tsAddress;

    private final int tsPort;

    private int index;

    private IOrder order;

    private AlgorithmPanel panel;

    public Algorithm(String name, Pusher trader, String tsAddress, int tsPort) {
        this.name = name;
        this.trader = trader;
        this.tsAddress = tsAddress;
        this.tsPort = tsPort;
    }

    public Algorithm(String name, Pusher trader) {
        this(name, trader, "localhost", 9090);
    }

    protected void setSecurity(String security) {
        this.security = security;
    }

    protected void setResolution(TimeFrame resolution) {
        this.resolution = resolution;
    }

    protected void setStart(LocalDate start) {
        this.start = start;
    }

    protected void setEnd(LocalDate end) {
        this.end = end;
    }

    protected void setModel(Model model) {
        this.model = model;
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected void setType(Order type) {
        this.type = type;
    }

    protected void setVerbose(boolean print) {
        this.print = print;
    }

    protected void conclude() {
        panel.writer.append(order.conclude());
    }

    protected void visPrice(Pair<String, List<Double>>...series){
        panel.visPrice(series);
    }

    protected void visProfit(Pair<String, List<Double>> profit){
        panel.visProfit(profit);
    }

    protected void log(String line){
        panel.writer.append(line);
    }

    protected List<Double> profit(){
        return order.profit();
    }

    protected boolean buy(String id, int share, double price) {
        String rtn = order.buy(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                panel.writer.append(rtn + " on the " + index + "th bar\n");
            return true;
        }
        return false;
    }

    protected boolean sell(String id, int share, double price) {
        String rtn = order.sell(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                panel.writer.append(rtn + " on the " + index + "th bar\n");
            return true;
        }
        return false;
    }

    protected boolean buyClose(String id, int share, double price) {
        String rtn = order.buyClose(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                panel.writer.append(rtn + " on the " + index + "th bar\n");
            return true;
        }
        return false;
    }

    protected boolean sellOpen(String id, int share, double price) {
        String rtn = order.sellOpen(id, share, price);
        if (!rtn.isEmpty()) {
            if (print)
                panel.writer.append(rtn + " on the " + index + "th bar\n");
            return true;
        }
        return false;
    }

    private void configure() {
        switch (model) {
            case TEST:
                order = new MockOrder(name);
                break;
            case TRADE: {
                order = state.equals(MOCK) ? new MockOrder(name) : new RealOrder(name, tsAddress, tsPort);
                break;
            }
        }
    }

    private List<? extends BaseData> download() {
        List<? extends BaseData> data = new ArrayList<>();
        switch (resolution) {
            case TICK:
                data = ticks(security, start, end);
                data.remove(0);
                break;
            case MIN_1:
            case MIN_5:
                data = bars(security, resolution, start, end);
                break;
        }
        return data;
    }

    private void simulate() {
        System.out.println("Algorithm [" + name + "] start to run");
        if (model.equals(TEST)) {
            List<? extends BaseData> data = download();
            for (index = 0; index < data.size(); index++)
                onData(data.get(index));
        } else
            trader.register(this);
    }

    public void run() {
        init();
        configure();
        simulate();
        onComplete();
    }

    public void setPanel(AlgorithmPanel panel){
        this.panel = panel;
    }

    public AlgorithmPanel getPanel(){
        return panel;
    }

    public double total() {
        return order.totalReturn();
    }

    public String getName() {
        return name;
    }

    public String getSecurity() {
        return security;
    }

    public TimeFrame getResolution() {
        return resolution;
    }

    public void onComplete() {
        order.conclude();
    }

    public abstract void init();

    public abstract void onData(BaseData data);
}
