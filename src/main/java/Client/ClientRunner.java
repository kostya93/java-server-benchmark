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
import static Common.Constants.*;

/**
 * Created by kostya on 08.12.2016.
 */

public class ClientRunner {
    private final String serverHost;
    private final int serverConfigPort;
    private final ClientType clientType;
    private final int serverProcessPort;
    private final int serverType;

    private final List<Thread> clients;
    private final LongAdder clientsTime = new LongAdder();
    private Socket socket;

    public ClientRunner(String serverHost,
                        int serverConfigPort,
                        int serverProcessPort,
                        ClientType clientType,
                        int serverType) {
        this.serverHost = serverHost;
        this.serverConfigPort = serverConfigPort;
        this.serverProcessPort = serverProcessPort;
        this.serverType = serverType;
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
                    getClient().run(serverHost, serverProcessPort, numOfElements, delta, numOfRequests);
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

    public boolean startServer() throws IOException {
        if (socket == null) {
            socket = new Socket(serverHost, serverConfigPort);
        }
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(ConfigureMessage.START_SERVER);
        dataOutputStream.writeInt(serverType);
        dataOutputStream.writeInt(serverProcessPort);
        dataOutputStream.flush();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        return dataInputStream.readBoolean();
    }

    public boolean resetServer() throws IOException {
        if (socket != null) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(ConfigureMessage.RESET_SERVER);
            dataOutputStream.flush();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            return dataInputStream.readBoolean();
        }
        return false;
    }

    public void exitServer() throws IOException {
        if (socket != null) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(ConfigureMessage.EXIT);
            dataOutputStream.flush();
            socket.close();
            socket = null;
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
        if (socket == null) {
            return null;
        }
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream.writeInt(ConfigureMessage.STATS);
        dataOutputStream.flush();
        Statistics statistics = new Statistics();
        statistics.setTimePerClientServer(dataInputStream.readLong() / numOfClient);
        statistics.setTimePerRequestServer(dataInputStream.readLong() / numOfRequests);
        statistics.setTimePerClient(clientsTime.longValue() / numOfClient);
        return statistics;
    }
}
