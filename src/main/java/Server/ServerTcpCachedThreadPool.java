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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.MessageType;
import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Server creates a task to communicate by TCP with each client,
 * and summit it to CachedThreadPool.
 */
public class ServerTcpCachedThreadPool implements Server {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private ExecutorService executor = Executors.newCachedThreadPool();

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
                executor.submit(() -> processClient(clientSocket));
            }
        } catch (Exception ignored) {
        }
    }

    private void processClient(Socket clientSocket) {
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
        reset();
    }

    private void executeArray(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int size = inputStream.readInt();
        byte[] data = new byte[size];
        inputStream.readFully(data);
        List<Integer> list = new ArrayList<>(Message.Array.parseFrom(data).getArrayList());

        long startSort = System.nanoTime()/NANOS_IN_MILLIS;
        List<Integer> sortedList = Server.sort(list);
        long endSort = System.nanoTime()/NANOS_IN_MILLIS;

        timeForClients.add(endSort - startSort);

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

    @Override
    public void reset() {
        timeForClients.reset();
        timeForRequests.reset();
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor = Executors.newCachedThreadPool();
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
