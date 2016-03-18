package tongtong.qiangqiang.func;

import cn.quanttech.quantera.common.type.data.BarInfo;
import cn.quanttech.quantera.common.type.data.BaseData;
import cn.quanttech.quantera.common.type.data.TickInfo;
import cn.quanttech.quantera.common.type.data.TimeFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import jwave.datatypes.natives.Complex;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015/11/18.
 */
public class GeneralUtilizer {

    public static double valueDefault(Object o) {
        double v = 0.;
        if (o instanceof Number)
            v = (Double) o;
        if (o instanceof BarInfo)
            v = ((BarInfo) o).close;
        if (o instanceof TickInfo)
            v = ((TickInfo) o).last;
        return v;
    }

    public static double value(Object o, String field) {
        return 0.;
    }

    public static List<Double> smooth(List<Double> input, int len1) {
        List<Double> smooth = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            if (i < len1)
                smooth.add(wma(input.subList(0, i + 1), defaultWeights(i + 1)));
            else
                smooth.add(wma(input.subList(i + 1 - len1, i + 1), defaultWeights(len1)));
        }
        return smooth;
    }

    public static double log2(double a) {
        return Math.log10(a) / Math.log10(2);
    }

    public static double sma(List<Double> price) {
        if (price.isEmpty()) return 0.0;
        return price.stream().mapToDouble(d -> d).sum() / price.size();
    }

    public static double ema(final List<Double> price, final int number) {
        if (price.isEmpty()) return 0.0;
        double k = 2.0 / (number + 1.0);
        double ema = price.get(0);
        for (int i = 1; i < price.size(); i++)
            ema = ema * (1 - k) + price.get(i) * k;
        return ema;
    }

    public static List<Double> defaultWeights(int N) {
        List<Double> res = new ArrayList<>();
        double sum = N * (N + 1) / 2.0;
        for (int i = 1; i <= N; i++)
            res.add(i / sum);
        return res;
    }

    public static List<Double> window(List<Double> data, int size) {
        List<Double> res = new LinkedList<>();
        for (int i = 0; i < data.size(); i += size) {
            int to = Math.min(data.size(), i + size);
            res.add(wma(data.subList(i, to), defaultWeights(to - i)));
        }
        return res;
    }

    public static Complex[] toComplex(List<Double> data) {
        Complex[] complex = new Complex[data.size()];
        for (int i = 0; i < data.size(); i++) {
            complex[i] = new Complex(data.get(i), 0);
        }
        return complex;
    }

    public static List<Double> toReal(Complex[] data) {
        List<Double> real = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
            real.add(data[i].getReal());
        return real;
    }

    public static List<Double> toMagnitude(Complex[] data) {
        List<Double> real = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
            real.add(data[i].getMag());
        return real;
    }

    public static double wma(final List<Double> price, final List<Double> weight) {
        double sum = 0.0;
        for (int i = 0; i < price.size(); i++)
            sum += price.get(i) * (i >= weight.size() ? weight.get(weight.size() - 1) : weight.get(i));
        return sum;
    }

    public static double max(final List<Double> price) {
        if (price.isEmpty()) return 0.0;
        return price.stream().max(Double::compareTo).get();
    }

    public static double min(final List<Double> price) {
        if (price.isEmpty()) return 0.0;
        return price.stream().min(Double::compareTo).get();
    }

    public static List<Double> extract(List<? extends BaseData> data, String field) {
        List<Double> res = new ArrayList<>();
        data.stream().forEach(b -> {
            Class c = b.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields)
                if (f.getName().equalsIgnoreCase(field))
                    try {
                        res.add(f.getDouble(b));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
        });
        return res;
    }

    public static BarInfo combine(BarInfo bar, TickInfo tick, TimeFrame resolution) {
        if (bar == null) {
            LocalDate date = tick.tradingTime.toLocalDate();
            LocalTime time = tick.tradingTime.toLocalTime();
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(time.getHour(), time.getMinute()));
            BarInfo newBar = new BarInfo(tick.secuCode, tick.exchangeID, dateTime, tick.tradingDay, resolution);
            newBar.open = newBar.high = newBar.low = newBar.close = tick.last;
            newBar.volume += tick.volume;
            return newBar;
        } else {
            LocalDateTime endTime = bar.tradingTime.plusMinutes(bar.resolution.value());
            if (tick.tradingTime.isBefore(endTime)) {
                bar.volume += tick.volume;
                bar.high = Math.max(bar.high, tick.last);
                bar.low = Math.min(bar.low, tick.last);
                bar.close = tick.last;
                return null;
            } else
                return combine(null, tick, resolution);
        }
    }

    public static FullHttpResponse buildRsp(HttpVersion version, HttpResponseStatus status, ByteBuf buf) {
        FullHttpResponse response = new DefaultFullHttpResponse(version, status, buf);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set("Access-Control-Allow-Origin", "http://192.168.10.11:8090");
        return response;
    }

    public static void sendString(ChannelHandlerContext ctx, String msg, HttpResponseStatus status){
        ChannelFuture f = ctx.writeAndFlush(buildRsp(HTTP_1_1, status, copiedBuffer(msg.getBytes(UTF_8))));
        f.addListener(ChannelFutureListener.CLOSE);
    }
}
