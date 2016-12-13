package Client;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static Client.Constants.ClientType;
import static Common.Constants.MessageType;
import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Created by kostya on 08.12.2016.
 */

public class ClientRunner {
    private final String serverHost;
    private final int serverPort;
    private final ClientType clientType;

    private final List<Thread> clients;
    private final LongAdder clientsTime = new LongAdder();

    public ClientRunner(String serverHost,
                        int serverPort,
                        ClientType clientType) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clients = new ArrayList<>();
        this.clientType = clientType;
    }

    public Statistics run(int numOfElements,
                          int numOfClient,
                          int delta,
                          int numOfRequests) throws IOException {
        for (int i = 0; i < numOfClient; i++) {
            clients.add(new Thread(() -> {
                try {
                    long start = System.nanoTime();
                    getClient().run(serverHost, serverPort, numOfElements, delta, numOfRequests);
                    long end = System.nanoTime();
                    clientsTime.add((end - start) / NANOS_IN_MILLIS);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Something wrong in Client.run()", e);
                }
            }));
        }
        clients.forEach(Thread::start);
        clients.forEach((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        clients.clear();
        return getStatistics(numOfClient, numOfRequests);
    }

    public void configureServer(int serverType) throws IOException {
        try (Socket socket = new Socket(serverHost, serverPort)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(serverType);
            outputStream.flush();
        }
    }

    private Client getClient() {
        switch (clientType) {
            case TCP_PERMANENT:
                return new ClientTcpPermanent();
            case TCP_NON_PERMANENT:
                return new ClientTcpNonPermanent();
            case UDP:
                return new ClientUdp();
        }
        throw new NotImplementedException();
    }

    private Statistics getStatistics(int numOfClient, int numOfRequests) throws IOException {
        switch (clientType) {
            case TCP_NON_PERMANENT:
            case TCP_PERMANENT:
                try (Socket socket = new Socket(serverHost, serverPort)) {
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeInt(MessageType.STATS);
                    outputStream.flush();
                    Statistics statistics = new Statistics();
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    statistics.setTimePerClientServer(dataInputStream.readLong() / numOfClient);
                    statistics.setTimePerRequestServer(dataInputStream.readLong() / numOfRequests);
                    statistics.setTimePerClient(clientsTime.longValue() / numOfClient);
                    return statistics;
                }
            case UDP:
                try (DatagramSocket datagramSocket = new DatagramSocket()) {
                    byte[] data = ByteBuffer.allocate(Integer.BYTES).putInt(MessageType.STATS).array();
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(serverHost), serverPort);
                    datagramSocket.send(packet);

                    data = new byte[Long.BYTES * 2];
                    packet.setData(data);
                    datagramSocket.receive(packet);

                    ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
                    Statistics statistics = new Statistics();
                    statistics.setTimePerClientServer(byteBuffer.getLong() / numOfClient);
                    statistics.setTimePerRequestServer(byteBuffer.getLong() / numOfRequests);
                    statistics.setTimePerClient(clientsTime.longValue() / numOfClient);
                    return statistics;
                }
        }
        throw new NotImplementedException();
    }
}
