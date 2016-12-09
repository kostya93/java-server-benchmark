package Server;

import Common.Message;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.MessageType;
import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Server creates a separate thread to communicate by TCP with each client.
 */
public class ServerTcpThreadImpl implements Server {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private List<Thread> clientThreads = new LinkedList<>();

    private final LongAdder timeForClients = new LongAdder();
    private final LongAdder timeForRequests = new LongAdder();

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }
    private void runServer() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Long startTime = System.nanoTime()/NANOS_IN_MILLIS;
                Thread clientThread = new Thread(() -> processClient(startTime, clientSocket));
                clientThreads.add(clientThread);
                clientThread.start();
            }
        } catch (Exception ignored) {}
    }
    private void processClient(long startClientTime, Socket clientSocket) {
        try {
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

            int messageType;
            while ((messageType = inputStream.readInt()) != MessageType.END) {
                switch (messageType) {
                    case MessageType.ARRAY:
                        long startRequestTime = System.nanoTime()/NANOS_IN_MILLIS;
                        executeArray(inputStream, outputStream);
                        long endRequestTime = System.nanoTime()/NANOS_IN_MILLIS;
                        timeForRequests.add(endRequestTime - startRequestTime);
                        break;
                    case MessageType.STATS:
                        executeStats(outputStream);
                        return;
                    default:
                        throw new NotImplementedException();
                }
            }
            long endClientTime = System.nanoTime()/NANOS_IN_MILLIS;
            timeForClients.add(endClientTime - startClientTime);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeStats(DataOutputStream outputStream) throws IOException {
        outputStream.writeLong(timeForClients.longValue());
        outputStream.writeLong(timeForRequests.longValue());
        outputStream.flush();
    }

    private void executeArray(InputStream inputStream, OutputStream outputStream) throws IOException {
        List<Integer> sortedList = Server.sort(
                new ArrayList<>(Message.Array.parseDelimitedFrom(inputStream).getArrayList())
        );
        Message.Array
                .newBuilder()
                .addAllArray(sortedList)
                .build()
                .writeDelimitedTo(outputStream);
        outputStream.flush();
    }

    @Override
    public void stop() throws IOException {
        if (serverSocket == null) {
            return;
        }

        serverSocket.close();
        clientThreads.forEach(Thread::interrupt);
        clientThreads.clear();
        serverThread.interrupt();
    }
}