package tongtong.qiangqiang.mind.order;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public RealOrder(String name, String tsIP, int tsPort) {
        super(name);
        this.tsIP = tsIP;
        this.tsPort = tsPort;
        this.baseUrl = "http://" + tsIP + ":" + tsPort + "/?";
    }

    @Override
    public String buy(String id, int share, double price) {
        String query = baseUrl + "type=market&direction=buy&action=open" + "&code=" + id + "&share=" + share + "&price=" + price;
        if (!lPos) {
            sendOrder(query);
            buyAction(price);
            return "\n[long   open]: " + price;
        }
        return "";
    }

    @Override
    public String sell(String id, int share, double price) {
        String query = baseUrl + "type=market&direction=sell&action=close" + "&code=" + id + "&share=" + share + "&price=" + price;

        if (lPos) {
            sendOrder(query);
            sellAction(price);
            profitChart.vis("HH:mm:ss", profit);
            return "[long  close]: " + price + ", delta: " + longProfit.getLast() + ", profit: " + lDif;
        }
        return "";
    }

    @Override
    public String buyClose(String id, int share, double price) {
        String query = baseUrl + "type=market&direction=buy&action=close" + "&code=" + id + "&share=" + share + "&price=" + price;
        if (sPos) {
            sendOrder(query);
            buyCloseAction(price);
            profitChart.vis("HH:mm:ss", profit);
            return "[short close]: " + price + ", delta: " + shortProfit.getLast() + ", profit: " + sDif;
        }
        return "";
    }

    @Override
    public String sellOpen(String id, int share, double price) {
        String query = baseUrl + "type=market&direction=sell&action=open" + "&code=" + id + "&share=" + share + "&price=" + price;
        if (!sPos) {
            sendOrder(query);
            sellOpenAction(price);
            return "\n[short  open]: " + price;
        }
        return "";
    }

    private void sendOrder(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection con = null;
            con = (HttpURLConnection) url.openConnection();
            con.connect();
            if (con.getResponseCode() != 200)
                System.out.println(con.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
