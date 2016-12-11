package Client;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
    private final int numOfElements;
    private final int numOfClient;
    private final int delta;
    private final int numOfRequests;
    private final ClientType clientType;

    private final List<Thread> clients;
    private final LongAdder clientsTime = new LongAdder();

    public ClientRunner(String serverHost,
                        int serverPort,
                        int numOfElements,
                        int numOfClient,
                        int delta,
                        int numOfRequests,
                        ClientType clientType) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.numOfElements = numOfElements;
        this.numOfClient = numOfClient;
        this.delta = delta;
        this.numOfRequests = numOfRequests;
        this.clients = new ArrayList<>(numOfClient);
        this.clientType = clientType;
    }

    public void run() {
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


    }

    private Client getClient() {
        switch (clientType) {
            case TCP_PERMANENT:
                return new ClientTcpPermanent();
            case TCP_NON_PERMANENT:
                return new ClientTcpNonPermanent();
        }
        throw new NotImplementedException();
    }

    public Statistics getStatistics() throws IOException {
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
    }
}
