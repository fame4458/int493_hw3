package com.example;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        ServerSocketChannel serverCh = ServerSocketChannel.open();
        serverCh.configureBlocking(false);
        serverCh.bind(new InetSocketAddress(9000));
        serverCh.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Listen for connection");
        while (true){
            selector.select(); //watching event
            System.out.println("Got some event");
            Set<SelectionKey> keys = selector.selectedKeys(); //list of event
            //do something with keys

            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()){
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel clientCh = ch.accept();
                    clientCh.configureBlocking(false);
                    clientCh.register(selector, SelectionKey.OP_READ);
                }

//                if (key.isWritable()){
//                    //client ready fo write
//                    SocketChannel ch = (SocketChannel) key.channel();
//                    ByteBuffer buff = ByteBuffer.allocate(20);
//                    String msg = String.format("TIME:%d\n", System.currentTimeMillis());
//                    buff.put(msg.getBytes());
//                    buff.flip();
//                    ch.write(buff);
//                    Thread.sleep(1000);
//                }

                if (key.isReadable()){
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(20);
                    int n = ch.read(buff);
                    ch.read(buff);

                    buff.flip();
                    System.out.println(new String(buff.array()));

                    String msg = String.format("TIME:%d\n", System.currentTimeMillis());
                    buff.clear();
                    buff.put(msg.getBytes());
                    buff.flip();
                    ch.write(buff);

                }
            }
        }

    }
}
