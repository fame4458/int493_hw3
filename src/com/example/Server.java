package com.example;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private  static ArrayList<SocketChannel> clients = new ArrayList<>();

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

                if (key.isAcceptable()){
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel clientCh = ch.accept();
                    clientCh.configureBlocking(false);
                    clientCh.register(selector, SelectionKey.OP_READ);
                    clients.add(clientCh);
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
                    ByteBuffer buff = ByteBuffer.allocate(256);
                    ch.configureBlocking(false);
                    buff.clear();
                    int n = ch.read(buff);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }

//                    ch.read(buff);
//
                    buff.flip();
                    String msg = new String(buff.array());

                    System.out.println("Message: "+ msg);

                    for (SocketChannel client: clients){
                        ByteBuffer wBuff = ByteBuffer.allocate(256);
                        wBuff.put(("Message = "+msg).getBytes());
                        wBuff.flip();
                        client.write(wBuff);
                    }
                }
                it.remove();

            }
        }

    }
}
