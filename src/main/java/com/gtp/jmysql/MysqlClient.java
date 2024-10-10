package com.gtp.jmysql;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlClient {

    public NettyClientHandler client = null;

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private String host;
    private Integer port;

    public MysqlClient(String host, int port) {
        this.host = host;
        this.port = port;

        start();
    }

    public void start() {
        client = new NettyClientHandler();
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new StringEncoder());
                        socketChannel.pipeline().addLast(client);
                    }
                });

        try {
            b.connect(host, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String message) throws InterruptedException, ExecutionException {
        client.sendMessage(message);
        return (String)executorService.submit(client).get();
    }

    static public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {
        private ChannelHandlerContext context;
        private String message;
        private String result;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            context = ctx;
        }

        @Override
        public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            result = msg.toString();
            notify();
        }

        public synchronized Object call() throws Exception {
            context.writeAndFlush(this.message);
            wait();
            return result;
        }

        public void sendMessage(String message) {
            this.message = message;
        }
    }

    public static void main(String[] args) throws InterruptedException, InterruptedException,
            ExecutionException, IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        MysqlClient client = new MysqlClient("localhost", 18080);
        terminal.writer().append("连接成功");

        String prompt = "mysql> ";
        while (true) {
            terminal.writer().append("\n");
            terminal.flush();
            final String line = lineReader.readLine(prompt);
            System.out.println(client.sendMessage(line));
        }
    }
}
