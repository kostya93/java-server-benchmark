package Server;

import Common.Message;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.MessageType;
import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Server creates a task to communicate by TCP with each client,
 * and summit it to CachedThreadPool.
 */
public class ServerTcpCachedThreadPoolImpl implements Server {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
                Long startTime = System.nanoTime() / NANOS_IN_MILLIS;
                executor.submit(() -> processClient(startTime, clientSocket));
            }
        } catch (Exception ignored) {
        }
    }

    private void processClient(long startClientTime, Socket clientSocket) {
        try {
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

            int messageType;
            while ((messageType = inputStream.readInt()) != MessageType.END_ARRAYS) {
                switch (messageType) {
                    case MessageType.ARRAY:
                        long startRequestTime = System.nanoTime() / NANOS_IN_MILLIS;
                        executeArray(inputStream, outputStream);
                        long endRequestTime = System.nanoTime() / NANOS_IN_MILLIS;
                        timeForRequests.add(endRequestTime - startRequestTime);
                        break;
                    case MessageType.STATS:
                        executeStats(outputStream);
                        return;
                    default:
                        throw new NotImplementedException();
                }
            }
            long endClientTime = System.nanoTime() / NANOS_IN_MILLIS;
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

    private void executeStats(OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeLong(timeForClients.longValue());
        dataOutputStream.writeLong(timeForRequests.longValue());
        dataOutputStream.flush();
    }

    private void executeArray(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int size = inputStream.readInt();
        byte[] data = new byte[size];
        inputStream.readFully(data);

        List<Integer> sortedList = Server.sort(
                new ArrayList<>(Message.Array.parseFrom(data).getArrayList())
        );
        Message.Array
                .newBuilder()
                .addAllArray(sortedList)
                .build()
                .writeTo(outputStream);
        outputStream.flush();
    }

    @Override
    public void stop() throws IOException {
        if (serverSocket == null) {
            return;
        }

        serverSocket.close();
        serverSocket = null;
        executor.shutdown();
        serverThread.interrupt();
    }
}
