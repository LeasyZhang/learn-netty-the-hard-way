package example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RedisClient {

    private static final String HOST = System.getProperty("host", "127.0.0.1");
    private static final int PORT = Integer.parseInt(System.getProperty("port", "6379"));

    public static void main(String[] args) throws InterruptedException, IOException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RedisDecoder());
                            pipeline.addLast(new RedisBulkStringAggregator());
                            pipeline.addLast(new RedisEncoder());
                            pipeline.addLast(new RedisArrayAggregator());
                            pipeline.addLast(new RedisClientHandler());
                        }
                    });
            Channel ch = bootstrap.connect(HOST, PORT).sync().channel();

            System.out.println("Enter Redis commands (quit to end)");
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            for (; ; ) {
                final String input = in.readLine();
                final String line = input == null ? null : input.trim();

                if (line == null || line.equals("quit")) {
                    ch.close().sync();
                    break;
                } else if (line.isEmpty()) {
                    continue;
                }

                lastWriteFuture = ch.writeAndFlush(line);
                lastWriteFuture.addListener((GenericFutureListener) (future) -> {
                    if (!future.isSuccess()) {
                        System.err.println("Write failed:");
                        future.cause().printStackTrace(System.err);
                    }
                });
            }

            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
