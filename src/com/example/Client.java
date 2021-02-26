package com.example;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        SocketChannel clientCh = SocketChannel.open();
        clientCh.configureBlocking(false);
        clientCh.register(selector, SelectionKey.OP_CONNECT);

        clientCh.connect(new InetSocketAddress("127.0.0.1", 9000));

        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                it.remove();
                //check or do something
                if (key.isConnectable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (!channel.finishConnect()){
                        channel.close();
                        continue;
                    }
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }

                if (key.isWritable()){
                    //client ready fo write
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(20);

//                    String msg = String.format("TIME:%d\n", System.currentTimeMillis());
                    buff.put("test".getBytes());
                    buff.flip();
                    ch.write(buff);
                    Thread.sleep(1000);
                }

                if (key.isReadable()){
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(20);
                    ch.read(buff);
                    buff.flip();
                    System.out.println(new String(buff.array()));
                }
            }
        }
    }
}
