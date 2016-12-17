import Client.ClientRunner;
import Client.Statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static Client.Constants.ClientType;
import static Common.Constants.ServerType;

/**
 * Created by kostya on 07.12.2016.
 */
public class ClientCli {
    private static class Param {
        final int regular;
        final int min;
        final int max;
        final int step;

        private Param(int regular, int min, int max, int step) {
            this.regular = regular;
            this.min = min;
            this.max = max;
            this.step = step;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final String serverHost = "192.168.1.33";
        final int configPort = 44444;
        final int processPort = 55555;

        final int numOfRequests = 15;

        final Param numOfClients = new Param(10, 1, 41, 5);
        final Param numOfElements = new Param(2000, 300, 6000, 500);
        final Param delta = new Param(5, 1, 101, 10);

        ClientRunner[] clientRunners = {
                new ClientRunner(serverHost, configPort, processPort, ClientType.TCP_PERMANENT, ServerType.TCP_ASYNC),
                new ClientRunner(serverHost, configPort, processPort, ClientType.TCP_PERMANENT, ServerType.TCP_CACHED_THREAD_POOL),
                new ClientRunner(serverHost, configPort, processPort, ClientType.TCP_PERMANENT, ServerType.TCP_NON_BLOCKING),
                new ClientRunner(serverHost, configPort, processPort, ClientType.TCP_NON_PERMANENT, ServerType.TCP_ONE_THREAD_SEQUENTIAL),
                new ClientRunner(serverHost, configPort, processPort, ClientType.TCP_PERMANENT, ServerType.TCP_THREAD),
                new ClientRunner(serverHost, configPort, processPort, ClientType.UDP, ServerType.UDP_THREAD),
                new ClientRunner(serverHost, configPort, processPort, ClientType.UDP, ServerType.UDP_TREAD_POOL)
        };

        List<List<Statistics>> resultsNumOfClients = new ArrayList<>();
        List<List<Statistics>> resultsNumElements = new ArrayList<>();
        List<List<Statistics>> resultsDelta = new ArrayList<>();

        for (ClientRunner clientRunner : clientRunners) {
            System.out.println("clientRunner");
            clientRunner.startServer();

            System.out.println("resNumOfClients");
            List<Statistics> resNumOfClients = new ArrayList<>();
            int numOfIteration = (numOfClients.max - numOfClients.min)/numOfClients.step + 1;
            for (int j = numOfClients.min, k = 1; j <= numOfClients.max; j += numOfClients.step, k++) {
                System.out.println(100.0*k/numOfIteration + " %");
                resNumOfClients.add(clientRunner.run(numOfElements.regular, j, delta.regular, numOfRequests));
                clientRunner.resetServer();
            }
            resultsNumOfClients.add(resNumOfClients);

            System.out.println("resNumOfElements");
            numOfIteration = (numOfElements.max - numOfElements.min)/numOfElements.step + 1;
            List<Statistics> resNumOfElements = new ArrayList<>();
            for (int j = numOfElements.min, k = 1; j <= numOfElements.max; j += numOfElements.step, k++) {
                System.out.println(100.0*k/numOfIteration + " %");
                resNumOfElements.add(clientRunner.run(j, numOfClients.regular, delta.regular, numOfRequests));
                clientRunner.resetServer();
            }
            resultsNumElements.add(resNumOfElements);

            System.out.println("resDelta");
            numOfIteration = (delta.max - delta.min)/delta.step + 1;
            List<Statistics> resDelta = new ArrayList<>();
            for (int j = delta.min, k = 1; j <= delta.max; j += delta.step, k++) {
                System.out.println(100.0*k/numOfIteration + " %");
                resDelta.add(clientRunner.run(numOfElements.regular, numOfClients.regular, j, numOfRequests));
                clientRunner.resetServer();
            }
            resultsDelta.add(resDelta);

            clientRunner.exitServer();
            Thread.sleep(1000);
        }

        writeResults("ClientServerByNumOfClients.txt", resultsNumOfClients, StatType.TIME_PER_CLIENT_SERVER);
        writeResults("RequestServerByNumOfClients.txt", resultsNumOfClients, StatType.TIME_PER_REQUEST_SERVER);
        writeResults("ClientByNumOfClients.txt", resultsNumOfClients, StatType.TIME_PER_CLIENT);

        writeResults("ClientServerByNumOfElements.txt", resultsNumElements, StatType.TIME_PER_CLIENT_SERVER);
        writeResults("RequestServerByNumOfElements.txt", resultsNumElements, StatType.TIME_PER_REQUEST_SERVER);
        writeResults("ClientByNumOfElements.txt", resultsNumElements, StatType.TIME_PER_CLIENT);

        writeResults("ClientServerByDelta.txt", resultsDelta, StatType.TIME_PER_CLIENT_SERVER);
        writeResults("RequestServerByDelta.txt", resultsDelta, StatType.TIME_PER_REQUEST_SERVER);
        writeResults("ClientByDelta.txt", resultsDelta, StatType.TIME_PER_CLIENT);

        writeParams("Params.txt", numOfClients, numOfElements, delta, numOfRequests);
    }

    private static void writeParams(String filename, Param numOfClients, Param numOfElements, Param delta, int numOfRequests) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(filename)) {
            printWriter.println(numOfClients.regular);
            printWriter.println(numOfClients.min);
            printWriter.println(numOfClients.max);
            printWriter.println(numOfClients.step);

            printWriter.println(numOfElements.regular);
            printWriter.println(numOfElements.min);
            printWriter.println(numOfElements.max);
            printWriter.println(numOfElements.step);

            printWriter.println(delta.regular);
            printWriter.println(delta.min);
            printWriter.println(delta.max);
            printWriter.println(delta.step);

            printWriter.println(numOfRequests);
        }
    }

    private enum StatType {
        TIME_PER_CLIENT_SERVER,
        TIME_PER_CLIENT,
        TIME_PER_REQUEST_SERVER
    }

    private static void writeResults(String filename, List<List<Statistics>> results, StatType statType) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(filename)) {
            printWriter.println(results.size());
            for (List<Statistics> stats : results) {
                printWriter.println(stats.size());
                for (Statistics stat : stats) {
                    switch (statType) {
                        case TIME_PER_CLIENT_SERVER:
                            printWriter.println(stat.getTimePerClientServer());
                            break;
                        case TIME_PER_CLIENT:
                            printWriter.println(stat.getTimePerClient());
                            break;
                        case TIME_PER_REQUEST_SERVER:
                            printWriter.println(stat.getTimePerRequestServer());
                            break;
                    }
                }
            }
        }
    }
}
