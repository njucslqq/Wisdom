package tongtong.qiangqiang.mind.order;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class RealOrder extends BaseOrder {

    public final String tradingServerAddress;

    public final int tradingServerPort;

    public final String baseUrl;

    public RealOrder(String name, String tradingServerAddress, int tradingServerPort) {
        super(name);
        this.tradingServerAddress = tradingServerAddress;
        this.tradingServerPort = tradingServerPort;
        this.baseUrl = "http://"+tradingServerAddress+":"+tradingServerPort+"/";
    }

    @Override
    public String buy(String id, int share, double price) {
        return null;
    }

    @Override
    public String sell(String id, int share, double price) {
        return null;
    }

    @Override
    public String buyClose(String id, int share, double price) {
        return null;
    }

    @Override
    public String sellOpen(String id, int share, double price) {
        return null;
    }

    @Override
    public double total() {
        return 0;
    }

    @Override
    public void conclude() {

    }
}
