package Server;

import Common.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.MessageType;
import static Common.Constants.NANOS_IN_MILLIS;

/**
 * The server performs a non-blocking processing.
 * Each request is processed by a fixed size thread pool.
 */
public class ServerTcpNonBlocking implements Server {
    private Selector selector;
    private Thread serverThread;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Object registerLock = new Object();
    private final LongAdder timeForClients = new LongAdder();
    private final LongAdder timeForRequests = new LongAdder();

    @Override
    public void start(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    private void runServer() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                //http://php.mandelson.org/mk3/index.php/2011/10/06/better-selectablechannel-registration-in-java-nio/
                synchronized (registerLock) {
                }
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ, new Holder());
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        Holder holder = (Holder) key.attachment();
                        if (holder.isStartRequest()) {
                            holder.setTimeStartRequest(System.nanoTime() / NANOS_IN_MILLIS);
                        }
                        if (holder.isReading()) {
                            holder.read(client);
                        }
                        switch (holder.getState()) {
                            case END_READING_TYPE: {
                                ByteBuffer byteBuffer = holder.getByteBuffer();
                                byteBuffer.rewind();
                                int messageType = byteBuffer.getInt();
                                switch (messageType) {
                                    case MessageType.ARRAY:
                                        holder.createBuffer(Integer.BYTES);
                                        holder.setState(Holder.State.READING_SIZE);
                                        break;
                                    case MessageType.STATS:
                                        holder.setState(Holder.State.WRITING_STATS);
                                        byteBuffer = ByteBuffer.allocate(Long.BYTES * 2);
                                        byteBuffer.putLong(timeForClients.longValue());
                                        byteBuffer.putLong(timeForRequests.longValue());
                                        byteBuffer.rewind();
                                        holder.setByteBuffer(byteBuffer);
                                        key.interestOps(SelectionKey.OP_WRITE);
                                        break;
                                    case MessageType.END_ARRAYS:
                                        long timeEndClient = System.nanoTime() / NANOS_IN_MILLIS;
                                        timeForClients.add(timeEndClient - holder.getTimeStartClient());
                                        client.close();
                                        break;
                                }
                                break;
                            }
                            case END_READING_SIZE: {
                                ByteBuffer byteBuffer = holder.getByteBuffer();
                                byteBuffer.rewind();
                                int size = byteBuffer.getInt();
                                holder.createBuffer(size);
                                holder.setState(Holder.State.READING_ARRAY);
                                break;
                            }
                            case END_READING_ARRAY:
                                executor.submit(() -> processClient(key));
                                break;
                        }
                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        Holder holder = (Holder) key.attachment();
                        if (holder.isWriting()) {
                            holder.write(client);
                        }
                        switch (holder.getState()) {
                            case END_WRITING_ARRAY:
                                holder.createBuffer(Integer.BYTES);
                                holder.setState(Holder.State.READING_TYPE);
                                key.interestOps(SelectionKey.OP_READ);
                                long timeEndRequest = System.nanoTime() / NANOS_IN_MILLIS;
                                timeForRequests.add(timeEndRequest - holder.getTimeStartRequest());
                                break;
                            case END_WRITING_STATS:
                                client.close();
                                break;
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClient(SelectionKey key) {
        try {
            SelectableChannel client = key.channel();
            Holder holder = (Holder) key.attachment();
            byte[] data = holder.getByteBuffer().array();
            List<Integer> sortedList = Server.sort(new ArrayList<>(Message.Array.parseFrom(data).getArrayList()));
            byte[] sortedData = Message.Array.newBuilder().addAllArray(sortedList).build().toByteArray();
            ByteBuffer byteBuffer = ByteBuffer.wrap(sortedData);
            byteBuffer.rewind();
            holder.setByteBuffer(byteBuffer);
            holder.setState(Holder.State.WRITING_ARRAY);
            synchronized (registerLock) {
                selector.wakeup();
                client.register(selector, SelectionKey.OP_WRITE, holder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws IOException {
        if (selector == null) {
            return;
        }
        selector.close();
        selector = null;
        serverThread.interrupt();
        executor.shutdown();
    }

}
