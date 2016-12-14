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
                byte[] data = new byte[Integer.BYTES];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

                serverSocket.receive(datagramPacket);
                InetAddress clientAddr = datagramPacket.getAddress();
                int clientPort = datagramPacket.getPort();
                DatagramSocket clientSocket = new DatagramSocket();

                if (ByteBuffer.wrap(datagramPacket.getData()).getInt() == MessageType.STATS) {
                    processStats(clientAddr, clientPort, clientSocket);
                    continue;
                }

                int portForClient = clientSocket.getLocalPort();

                data = ByteBuffer.allocate(Integer.BYTES).putInt(portForClient).array();
                datagramPacket.setData(data);
                serverSocket.send(datagramPacket);

                executor.submit(() -> processClient(clientAddr, clientPort, clientSocket));
            }
        } catch (IOException ignored) {
        }
    }

    private void processClient(InetAddress clientAddr, int clientPort, DatagramSocket clientSocket) {
        try {
            int messageType;
            while ((messageType = getIntFromClient(clientAddr, clientPort, clientSocket)) != Constants.MessageType.END_ARRAYS) {
                if (messageType == MessageType.ARRAY) {
                    long timeStartClient = System.nanoTime() / NANOS_IN_MILLIS;
                    int size = getIntFromClient(clientAddr, clientPort, clientSocket);
                    byte[] data = new byte[size];
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, clientAddr, clientPort);
                    clientSocket.receive(datagramPacket);
                    List<Integer> list = new ArrayList<>(Message.Array.parseFrom(data).getArrayList());
                    long timeStartSort = System.nanoTime() / NANOS_IN_MILLIS;
                    List<Integer> sortedList = Server.sort(list);
                    long timeEndSort = System.nanoTime() / NANOS_IN_MILLIS;
                    timeForClients.add(timeEndSort - timeStartSort);
                    data = Message.Array.newBuilder().addAllArray(sortedList).build().toByteArray();
                    datagramPacket.setData(data);
                    clientSocket.send(datagramPacket);
                    long timeEndClient = System.nanoTime() / NANOS_IN_MILLIS;
                    timeForRequests.add(timeEndClient - timeStartClient);
                } else {
                    throw new NotImplementedException();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
    }

    private int getIntFromClient(InetAddress clientAddr, int clientPort, DatagramSocket clientSocket) throws IOException {
        byte[] data = new byte[Integer.BYTES];
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, clientAddr, clientPort);
        clientSocket.receive(datagramPacket);
        return ByteBuffer.wrap(datagramPacket.getData()).getInt();
    }

    private void processStats(InetAddress clientAddr, int clientPort, DatagramSocket clientSocket) throws IOException {
        byte[] data = ByteBuffer.allocate(Long.BYTES * 2)
                .putLong(timeForClients.longValue())
                .putLong(timeForRequests.longValue())
                .array();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, clientAddr, clientPort);
        clientSocket.send(datagramPacket);
        clientSocket.close();
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
