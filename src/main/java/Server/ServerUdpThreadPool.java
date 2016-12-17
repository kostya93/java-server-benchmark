package Server;

import Common.Constants;
import Common.Message;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.MessageType;
import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Created by kostya on 11.12.2016.
 */
class ServerUdpThreadPool implements Server {
    private static final int MAX_MESSAGE_SIZE = 200_000;
    private DatagramSocket serverSocket;
    private ExecutorService executor;
    private Thread serverThread;

    private final LongAdder timeForClients = new LongAdder();
    private final LongAdder timeForRequests = new LongAdder();

    @Override
    public void start(int port) throws IOException {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        serverSocket = new DatagramSocket(port);
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    private void runServer() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] data = new byte[MAX_MESSAGE_SIZE];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                serverSocket.receive(datagramPacket);
                executor.submit(() -> processClient(datagramPacket));
            }
        } catch (IOException ignored) {
        }
    }

    private void processClient(DatagramPacket datagramPacket) {
        try {
            long timeStartClient = System.nanoTime() / NANOS_IN_MILLIS;
            byte[] data = Arrays.copyOfRange(datagramPacket.getData(), 0, datagramPacket.getLength());
            List<Integer> list = new ArrayList<>(Message.Array.parseFrom(data).getArrayList());

            long timeStartSort = System.nanoTime() / NANOS_IN_MILLIS;
            List<Integer> sortedList = Server.sort(list);
            long timeEndSort = System.nanoTime() / NANOS_IN_MILLIS;
            timeForClients.add(timeEndSort - timeStartSort);

            data = Message.Array.newBuilder().addAllArray(sortedList).build().toByteArray();
            datagramPacket.setData(data);
            serverSocket.send(datagramPacket);

            long timeEndClient = System.nanoTime() / NANOS_IN_MILLIS;
            timeForRequests.add(timeEndClient - timeStartClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws IOException {
        if (serverSocket == null) {
            return;
        }

        serverThread.interrupt();
        serverThread = null;
        executor.shutdown();
        executor = null;
        serverSocket.close();
        serverSocket = null;
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
