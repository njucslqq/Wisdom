package tongtong.qiangqiang.mind.trade;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tongtong.qiangqiang.mind.Algorithm;

import static io.netty.buffer.PooledByteBufAllocator.DEFAULT;
import static io.netty.channel.ChannelOption.*;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-26.
 */
public class Trader {

    public final Algorithm algorithm;

    public final int port;

    public Trader(Algorithm algorithm, int port) {
        this.algorithm = algorithm;
        this.port = port;
    }

    public void run() {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker);
            b.channel(NioServerSocketChannel.class);
            b.option(SO_KEEPALIVE, false);
            b.option(TCP_NODELAY, true);
            b.option(ALLOCATOR, DEFAULT);
            b.childHandler(new TraderInitializer(algorithm));
            b.childOption(ALLOCATOR, DEFAULT);

            Channel ch = b.bind(8080).sync().channel();

            System.out.println("\ntrader is now ready to accept connections on port 8080 \n");

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
