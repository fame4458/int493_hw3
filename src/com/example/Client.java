package com.example;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        SocketChannel clientCh = SocketChannel.open();
        clientCh.configureBlocking(false);
        clientCh.register(selector, SelectionKey.OP_CONNECT);

        Scanner scanner = new Scanner(System.in);
        ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());

        clientCh.connect(new InetSocketAddress("127.0.0.1", 9000));


        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                //check or do something
                if (key.isConnectable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    channel.finishConnect();
//                    if (!channel.finishConnect()){
//                        channel.close();
//                        continue;
//                    }
                    ByteBuffer buff = ByteBuffer.allocate(256);
                    buff.flip();
                    channel.write(buff);

                    executorService.submit(() -> {
                        while (true){
                            buff.clear();
                            System.out.print("Input: ");
                            String message = scanner.nextLine();
                            buff.put(message.getBytes());
                            buff.flip();
                            channel.write(buff);
                        }
                    });

                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                }

//                if (key.isWritable()){
//                    //client ready fo write
//                    SocketChannel ch = (SocketChannel) key.channel();
//                    ByteBuffer buff = ByteBuffer.allocate(20);
//
////                    String msg = String.format("TIME:%d\n", System.currentTimeMillis());
//                    buff.put("test".getBytes());
//                    buff.flip();
//                    ch.write(buff);
//                    Thread.sleep(1000);
//                }

                if (key.isReadable()){
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(256);
                    int n = ch.read(buff);
                    if (n == -1){
                        ch.close();
                        continue;
                    }
                    buff.flip();
                    System.out.println(new String(buff.array()).trim());
                }
                it.remove();

            }
        }
    }
}
