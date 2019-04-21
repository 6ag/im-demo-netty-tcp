package com.kefu.netty.server;

import com.kefu.netty.codec.PacketCodecHandler;
import com.kefu.netty.codec.Spliter;
import com.kefu.netty.server.handler.AuthHandler;
import com.kefu.netty.server.handler.CreateGroupRequestHandler;
import com.kefu.netty.server.handler.GroupMessageRequestHandler;
import com.kefu.netty.server.handler.HeartBeatRequestHandler;
import com.kefu.netty.handler.IMIdleStateHandler;
import com.kefu.netty.server.handler.JoinGroupRequestHandler;
import com.kefu.netty.server.handler.ListGroupMembersRequestHandler;
import com.kefu.netty.server.handler.LoginRequestHandler;
import com.kefu.netty.server.handler.LogoutRequestHandler;
import com.kefu.netty.server.handler.MessageRequestHandler;
import com.kefu.netty.server.handler.QuitGroupRequestHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 引导Netty服务器
 *
 * @author feng
 * @date 2019-04-20
 */
public class Server {

    private static final int PORT = 8888;

    /**
     * 开始引导服务器
     * 注意：不带 child 的是设置服务端的 Channel，带 child 的方法是设置每一条连接
     */
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                // 指定线程模型，这里是主从线程模型
                .group(bossGroup, workerGroup)
                // 指定服务端的 Channel 的 I/O 模型
                .channel(NioServerSocketChannel.class)
                // 给每条连接开启 TCP 底层心跳机制
//                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 给每条连接关闭 Nagle 算法
//                .childOption(ChannelOption.TCP_NODELAY, true)
                // 临时存放已完成三次握手的请求的列队的最大长度
//                .option(ChannelOption.SO_BACKLOG, 1024)
                // 指定服务器端启动过程中的逻辑，一般用不到
//                .handler(new ChannelInitializer<NioServerSocketChannel>() {
//                    @Override
//                    protected void initChannel(NioServerSocketChannel ch) throws Exception {
//                        System.out.println("服务器端启动中");
//                    }
//                })
                // 指定处理新连接数据的读写处理逻辑:每次有新连接到来，都会去执行ChannelInitializer.initChannel()，并new一大堆handler。所以如果handler中无成员变量，则可写成单例
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 空闲检测
                        ch.pipeline().addLast(new IMIdleStateHandler());
                        // 处理粘包半包
                        ch.pipeline().addLast(new Spliter());
                        // 数据包编解码器
                        ch.pipeline().addLast(PacketCodecHandler.INSTANCE);
                        // 登录
                        ch.pipeline().addLast(LoginRequestHandler.INSTANCE);
                        // 心跳检测
                        ch.pipeline().addLast(HeartBeatRequestHandler.INSTANCE);
                        // 身份校验
                        ch.pipeline().addLast(AuthHandler.INSTANCE);

                        // 单聊消息
                        ch.pipeline().addLast(MessageRequestHandler.INSTANCE);
                        // 创建群聊
                        ch.pipeline().addLast(CreateGroupRequestHandler.INSTANCE);
                        // 加入群组
                        ch.pipeline().addLast(JoinGroupRequestHandler.INSTANCE);
                        // 退出群组
                        ch.pipeline().addLast(QuitGroupRequestHandler.INSTANCE);
                        // 获取群成员
                        ch.pipeline().addLast(ListGroupMembersRequestHandler.INSTANCE);
                        // 群消息
                        ch.pipeline().addLast(GroupMessageRequestHandler.INSTANCE);
                        // 退出登录
                        ch.pipeline().addLast(LogoutRequestHandler.INSTANCE);
                    }
                });

        serverBootstrap.bind(PORT).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("端口绑定成功 port = " + PORT);
            } else {
                System.out.println("端口绑定失败");
            }
        });
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
