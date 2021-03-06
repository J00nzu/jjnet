package com.jjneko.jjnet.networking.pipes.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedList;

import com.jjneko.jjnet.networking.EndPoint;
import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.http.client.WebSocketClientHandler;
import com.jjneko.jjnet.networking.http.server.WebSocketHttpServerHandler;
import com.jjneko.jjnet.networking.pipes.Pipe;

public class SimpleHttpClientPipe extends Pipe{
	
	
	public WebSocketClientHandler channel;
	public Bootstrap clientBootstrap;
	public Channel ch;
	public EventLoopGroup group;
	
	public SimpleHttpClientPipe(final InetAddress address, final int serverPort) {
		super(null);
		handshake = new SimpleHttpClientPipeInitializer(this, address, serverPort);
		sendKeepAlive=false;
	}

	@Override
	public void send(byte[] message) {
		if(!ch.isActive()){
			close();
			return;
		}
		
//		channel.channel().writeAndFlush(message);
		
		WebSocketFrame frame=null;
		try {
			frame = new TextWebSocketFrame(new String(message, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        ch.writeAndFlush(frame);
	}

	@Override
	public byte[] receive(){
		if(connected && !queue.isEmpty())
			return queue.remove();
		else return null;
	}
	
	byte[] receiveHandshake(){
		if(!queue.isEmpty())
			return queue.remove();
		return null;
	}
	
	public void queuePacket(byte[] packet){
		queue.add(packet);
	}

	@Override
	public void sendKeepAlive() {
		
	}

	@Override
	public void close() {
		group.shutdownGracefully();
		ch.close();
		JJnet.removePipe(this);
	}

	@Override
	public String getIPAddress() {
		return ((InetSocketAddress)ch.remoteAddress()).getAddress().getHostAddress();
	}
}
