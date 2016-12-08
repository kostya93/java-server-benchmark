package Client;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

import static Client.Constants.ClientType;

/**
 * Created by kostya on 08.12.2016.
 */

public class ClientRunner {
    private final static int NANOS_IN_MILLIS = 1_000_000;

    private final String serverHost;
    private final int serverPort;
    private final int numOfElements;
    private final int numOfClient;
    private final int delta;
    private final int numOfRequests;
    private final ClientType clientType;

    private final List<Thread> clients;

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

    public long run() {
        Long[] millisForClient = new Long[numOfClient];
        for (int i = 0; i < numOfClient; i++) {
            int finalI = i;
            clients.add(new Thread(() -> {
                try {
                    long start = System.nanoTime();
                    getClient().run(serverHost, serverPort, numOfElements, delta, numOfRequests);
                    long end = System.nanoTime();
                    millisForClient[finalI] = (end - start)/NANOS_IN_MILLIS;
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
        return average(millisForClient);
    }

    private Client getClient() {
        switch (clientType) {
            case TCP_PERMANENT:
                return new ClientTcpPermanent();
        }
        throw new NotImplementedException();
    }

    private long average(Long[] arr) {
        OptionalDouble optionalDouble = Arrays.stream(arr).mapToDouble(it -> it).average();
        return optionalDouble.isPresent() ? (long) optionalDouble.getAsDouble() : -1L;
    }
}
