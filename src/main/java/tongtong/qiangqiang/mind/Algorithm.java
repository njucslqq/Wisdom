package tongtong.qiangqiang.mind;

import cn.quanttech.quantera.common.datacenter.HistoricalData;
import cn.quanttech.quantera.common.factor.Indicator;
import cn.quanttech.quantera.common.type.data.BaseData;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import org.apache.commons.lang3.tuple.Pair;
import tongtong.qiangqiang.mind.MindType.Model;
import tongtong.qiangqiang.mind.app.AlgorithmPanel;
import tongtong.qiangqiang.mind.order.IOrder;
import tongtong.qiangqiang.mind.order.MockOrder;
import tongtong.qiangqiang.mind.order.RealOrder;
import tongtong.qiangqiang.mind.push.Pusher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static tongtong.qiangqiang.mind.MindType.Model.TEST;
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

    private int share = 1;

    private Model model = TEST;

    private State state = MOCK;

    private boolean print = false;

    private final String name;

    private final double commision;

    private final Pusher trader;

    private final String tsAddress;

    private final int tsPort;

    private int index;

    private IOrder order;

    private AlgorithmPanel panel;

    public Algorithm(String name, double commision, Pusher trader, String tsAddress, int tsPort) {
        this.name = name;
        this.commision = commision;
        this.trader = trader;
        this.tsAddress = tsAddress;
        this.tsPort = tsPort;
    }

    public Algorithm(String name, double commision, Pusher trader) {
        this(name, commision, trader, "localhost", 9090);
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

    protected void setVerbose(boolean print) {
        this.print = print;
    }

    protected void setShare(int share) {
        this.share = share;
    }

    protected void conclude() {
        panel.writer.append(order.conclude());
    }

    protected void visPrice(Pair<String, List<Double>>... series) {
        panel.visPrice(series);
    }

    protected void visProfit(Pair<String, List<Double>>... profit) {
        panel.visProfit(profit);
    }

    protected void visPrice(int lastn, Indicator<?>... indicators) {
        visPrice(toNameValuePair(lastn, indicators));
    }

    protected void visProfit(int lastn, Indicator<?>... indicators) {
        visProfit(toNameValuePair(lastn, indicators));
    }

    private Pair[] toNameValuePair(int lastn, Indicator<?>... indicators) {
        Pair[] args = new Pair[indicators.length];
        for (int i = 0; i < indicators.length; i++)
            if (indicators[i].size() >= lastn)
                args[i] = Pair.of(indicators[i].getName(), indicators[i].getPrimary().lastn(lastn));
            else
                args[i] = Pair.of(indicators[i].getName(), indicators[i].getPrimary().all());
        return args;
    }

    protected void log(String str) {
        panel.writer.append(str);
    }

    protected void logl(String line) {
        panel.writer.append(line + "\n");
    }

    protected List<Double> profit() {
        return order.profit();
    }

    protected double lfp(double lastPrice) {
        return order.floatLongProfit(lastPrice);
    }

    protected double sfp(double lastPrice) {
        return order.floatShortProfit(lastPrice);
    }

    protected boolean buy(double price) {
        return tradingRecord(order.buy(security, share, price));
    }

    protected boolean sell(double price) {
        return tradingRecord(order.sell(security, share, price));
    }

    protected boolean buyClose(double price) {
        return tradingRecord(order.buyClose(security, share, price));
    }

    protected boolean sellOpen(double price) {
        return tradingRecord(order.sellOpen(security, share, price));
    }

    protected boolean sellSilent(double price) {
        return tradingRecord(order.sellSilent(security, share, price));
    }

    protected boolean buyCloseSilent(double price) {
        return tradingRecord(order.buyCloseSilent(security, share, price));
    }

    private boolean tradingRecord(String record) {
        if (!record.isEmpty()) {
            if (print)
                logl(record);
            return true;
        }
        return false;
    }

    private void configure() {
        switch (model) {
            case TEST:
                order = new MockOrder(commision);
                break;
            case TRADE: {
                order = state.equals(MOCK) ? new MockOrder(commision) : new RealOrder(commision, tsAddress, tsPort);
                break;
            }
        }
    }

    private List<? extends BaseData> download() {
        List<? extends BaseData> data = new ArrayList<>();
        switch (resolution) {
            case TICK:
                data = HistoricalData.ticks(security, start, end);
                data.remove(0);
                break;
            default:
                data = HistoricalData.bars(security, resolution, start, end);
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

    public void setPanel(AlgorithmPanel panel) {
        this.panel = panel;
    }

    public AlgorithmPanel getPanel() {
        return panel;
    }

    public double totalReturn() {
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
        conclude();
    }

    public abstract void init();

    public abstract void onData(BaseData data);
}
