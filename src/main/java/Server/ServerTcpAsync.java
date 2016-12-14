package Server;

import Common.Constants;
import Common.Message;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Server processes requests asynchronously
 */
class ServerTcpAsync implements Server {
    private final LongAdder timeForClients = new LongAdder();
    private final LongAdder timeForRequests = new LongAdder();

    private AsynchronousServerSocketChannel listener;
    private Thread serverThread;

    @Override
    public void start(int port) throws IOException {
        listener = AsynchronousServerSocketChannel.open();
        listener.bind(new InetSocketAddress(port));
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    private void runServer() {
        listener.accept(listener, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
            @Override
            public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel listener) {
                System.out.println("accept completed");
                listener.accept(listener, this);
                startRead(new Holder(), client);
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                System.out.println("accept failed");
            }
        });

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {
        }
    }

    private void startRead(Holder holder, AsynchronousSocketChannel client) {
        holder.setTimeStartRequest(System.nanoTime() / NANOS_IN_MILLIS);
        client.read(holder.getByteBuffer(), holder, new CompletionHandler<Integer, Holder>() {
            @Override
            public void completed(Integer result, Holder holder) {
                holder.checkState();
                if (holder.isReading()) {
                    client.read(holder.getByteBuffer(), holder, this);
                    return;
                }
                switch (holder.getState()) {
                    case END_READING_TYPE:
                        ByteBuffer byteBuffer = holder.getByteBuffer();
                        byteBuffer.rewind();
                        int messageType = byteBuffer.getInt();
                        switch (messageType) {
                            case Constants.MessageType.ARRAY:
                                holder.createBuffer(Integer.BYTES);
                                holder.setState(Holder.State.READING_SIZE);
                                client.read(holder.getByteBuffer(), holder, this);
                                break;
                            case Constants.MessageType.END_ARRAYS:
                                break;
                        }
                        break;
                    case END_READING_SIZE:
                        ByteBuffer sizeBuffer = holder.getByteBuffer();
                        sizeBuffer.rewind();
                        int size = sizeBuffer.getInt();
                        holder.createBuffer(size);
                        holder.setState(Holder.State.READING_ARRAY);
                        client.read(holder.getByteBuffer(), holder, this);
                        break;
                    case END_READING_ARRAY:
                        byte[] data = holder.getByteBuffer().array();

                        List<Integer> list;
                        try {
                            list = new ArrayList<>(Message.Array.parseFrom(data).getArrayList());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            break;
                        }
                        System.out.println("start sort");
                        long startSort = System.nanoTime() / NANOS_IN_MILLIS;
                        List<Integer> sortedList = Server.sort(list);
                        long endSort = System.nanoTime() / NANOS_IN_MILLIS;
                        System.out.println("end sort");
                        timeForClients.add(endSort - startSort);

                        holder.setByteBuffer(ByteBuffer.wrap(Message.Array
                                .newBuilder().addAllArray(sortedList).build().toByteArray()));
                        holder.getByteBuffer().rewind();
                        holder.setState(Holder.State.WRITING_ARRAY);
                        startWrite(holder, client);
                        break;
                }
            }

            @Override
            public void failed(Throwable exc, Holder holder) {
                System.out.println("read failed");
            }
        });
    }

    private void startWrite(Holder holder, AsynchronousSocketChannel client) {
        client.write(holder.getByteBuffer(), holder, new CompletionHandler<Integer, Holder>() {
            @Override
            public void completed(Integer result, Holder holder) {
                holder.checkState();
                if (holder.isWriting()) {
                    client.write(holder.getByteBuffer(), holder, this);
                    return;
                }
                if (holder.getState() == Holder.State.END_WRITING_ARRAY) {
                    holder.createBuffer(Integer.BYTES);
                    holder.setState(Holder.State.READING_TYPE);
                    long timeEndRequest = System.nanoTime() / NANOS_IN_MILLIS;
                    timeForRequests.add(timeEndRequest - holder.getTimeStartRequest());
                    startRead(holder, client);
                }
            }

            @Override
            public void failed(Throwable exc, Holder attachment) {
                System.out.println("write failed");
            }
        });
    }

    @Override
    public void stop() throws IOException {
        if (listener == null) {
            return;
        }

        serverThread.interrupt();
        listener.close();
        listener = null;
        timeForClients.reset();
        timeForRequests.reset();
    }

    @Override
    public long getTimeForClients() {
        return timeForClients.longValue();
    }

    @Override
    public long getTimeForRequests() {
        return timeForRequests.longValue();
    }
}
