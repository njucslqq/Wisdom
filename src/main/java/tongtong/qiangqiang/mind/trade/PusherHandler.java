package tongtong.qiangqiang.mind.trade;

import cn.quanttech.quantera.common.data.BarInfo;
import cn.quanttech.quantera.common.data.TickInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import tongtong.qiangqiang.func.GeneralUtilizer;
import tongtong.qiangqiang.mind.Algorithm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.time.LocalDate.now;
import static java.time.LocalDateTime.of;
import static java.time.LocalTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static tongtong.qiangqiang.func.GeneralUtilizer.combine;
import static tongtong.qiangqiang.func.GeneralUtilizer.sendString;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class PusherHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static final DateTimeFormatter FMT_TICK = ofPattern("HH:mm:ss.S");

    public static final DateTimeFormatter FMT_PRINT = ofPattern("yyyy-MM-dd HH:mm");

    public static final LocalTime AM_START = LocalTime.of(9, 0, 0);

    public static final LocalTime AM_END = LocalTime.of(11, 30, 0);

    public static final LocalTime PM_START = LocalTime.of(13, 30, 0);

    public static final LocalTime PM_END = LocalTime.of(15, 0, 0);

    public static final LocalTime NIGHT_START = LocalTime.of(21, 0, 0);

    public static final LocalTime NIGHT_END = LocalTime.of(2, 0, 0);

    public final Map<String, List<Algorithm>> algorithms;

    public final Map<Algorithm, BarInfo> currentBar;

    public PusherHandler(Map<String, List<Algorithm>> algorithms, Map<Algorithm, BarInfo> currentBar) {
        this.algorithms = algorithms;
        this.currentBar = currentBar;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //System.out.println("tick coming");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //System.out.println("closed");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        sendString(ctx, "exception caught", OK);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            Map<String, List<String>> para = new QueryStringDecoder(request.getUri()).parameters();

            if (para == null || para.isEmpty()) {
                sendString(ctx, "empty url", OK);
                return;
            }

            LocalTime now = LocalTime.now();
            if ((now.isAfter(NIGHT_END) && now.isBefore(AM_START)) || (now.isAfter(AM_END) && now.isBefore(PM_START)) || (now.isAfter(PM_END) && now.isBefore(NIGHT_START))) {
                sendString(ctx, "not trading time", OK);
                return;
            }

            TickInfo tick = new TickInfo(get(para, "code"), null, of(now(), parse(get(para, "time"), FMT_TICK)), null,
                    parseDouble(get(para, "last")), parseDouble(get(para, "ask")), parseDouble(get(para, "bid")),
                    parseInt(get(para, "volume")), parseDouble(get(para, "up")), parseDouble(get(para, "down"))
            );
            sendString(ctx, "accepted", OK);

            if (!algorithms.containsKey(tick.secuCode))
                return;

            algorithms.get(tick.secuCode).parallelStream().forEach(a -> {
                a.getPanel().writer.append(".");
                BarInfo newBar = combine(currentBar.get(a), tick, a.getResolution());
                if (newBar != null) {
                    if (currentBar.get(a) != null) {
                        a.getPanel().writer.append("\nBar Time = " + currentBar.get(a).tradingTime.format(FMT_PRINT) + "\n");
                        a.onData(currentBar.get(a));
                    }
                    currentBar.remove(a);
                    currentBar.put(a, newBar);
                }
            });
        }
    }

    String get(Map<String, List<String>> uri, String key) {
        return uri.get(key).get(0);
    }
}
