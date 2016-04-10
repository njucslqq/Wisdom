package tongtong.qiangqiang.mind.order;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class RealOrder extends BaseOrder {

    public final String tsIP;

    public final int tsPort;

    public final String baseUrl;

    public RealOrder(double commision, String tsIP, int tsPort) {
        super(commision);
        this.tsIP = tsIP;
        this.tsPort = tsPort;
        this.baseUrl = "http://" + tsIP + ":" + tsPort + "/?";
    }

    @Override
    public String buy(String id, int share, double price) {
        if (!stop && !lPos) {
            String query = baseUrl + "type=limit&direction=buy&action=open" + "&code=" + id + "&share=" + share + "&price=" + price;
            sendOrder(query);
            buyAction(price);
            return "\n[long   open]: " + price;
        }
        return "";
    }

    @Override
    public String sell(String id, int share, double price) {
        if (lPos) {
            String action = tradingDay().isEqual(lDate) ? "closeToday" : "close";
            String query = baseUrl + "type=limit&direction=sell&action=" + action + "&code=" + id + "&share=" + share + "&price=" + price;
            sendOrder(query);
            sellAction(price);
            return "[long  close]: " + price + ", delta: " + longProfit.last(0) + ", longProfit: " + lDif + ", totalProfit: " + totalReturn();
        }
        return "";
    }

    @Override
    public String buyClose(String id, int share, double price) {
        if (sPos) {
            String action = tradingDay().isEqual(sDate) ? "closeToday" : "close";
            String query = baseUrl + "type=limit&direction=buy&action=" + action + "&code=" + id + "&share=" + share + "&price=" + price;
            sendOrder(query);
            buyCloseAction(price);
            return "[short close]: " + price + ", delta: " + shortProfit.last(0) + ", shortProfit: " + sDif + ", totalProfit: " + totalReturn();
        }
        return "";
    }

    @Override
    public String sellOpen(String id, int share, double price) {
        if (!stop && !sPos) {
            String query = baseUrl + "type=limit&direction=sell&action=open" + "&code=" + id + "&share=" + share + "&price=" + price;
            sendOrder(query);
            sellOpenAction(price);
            return "\n[short  open]: " + price;
        }
        return "";
    }

    @Override
    public String sellSilent(String id, int share, double price) {
        String action = tradingDay().isEqual(lDate) ? "closeToday" : "close";
        String query = baseUrl + "type=limit&direction=sell&action=" + action + "&code=" + id + "&share=" + share + "&price=" + price;
        sendOrder(query);
        return "=========> [long  close]: stop loss " + floatLongProfit(price);
    }

    @Override
    public String buyCloseSilent(String id, int share, double price) {
        String action = tradingDay().isEqual(sDate) ? "closeToday" : "close";
        String query = baseUrl + "type=limit&direction=buy&action=" + action + "&code=" + id + "&share=" + share + "&price=" + price;
        sendOrder(query);
        return "=========> [short close]: stop loss " + floatShortProfit(price);
    }

    private void sendOrder(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            if (con.getResponseCode() != 200)
                System.out.println(con.getResponseMessage());
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
