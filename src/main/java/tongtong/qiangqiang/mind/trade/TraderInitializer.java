package tongtong.qiangqiang.mind.trade;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import tongtong.qiangqiang.mind.MockBase;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2016/2/26.
 */
public class TraderInitializer extends ChannelInitializer<SocketChannel>{

    public final MockBase algorithm;

    public TraderInitializer(MockBase algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpResponseEncoder());
        p.addLast(new TraderHandler(algorithm));
    }
}
