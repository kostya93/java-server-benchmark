package Server;

import Common.Message;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
class ServerTcpThread implements Server {
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
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> processClient(clientSocket));
                clientThreads.add(clientThread);
                clientThread.start();
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
                if (messageType == MessageType.ARRAY) {
                    long startRequestTime = System.nanoTime() / NANOS_IN_MILLIS;
                    executeArray(inputStream, outputStream);
                    long endRequestTime = System.nanoTime() / NANOS_IN_MILLIS;
                    timeForRequests.add(endRequestTime - startRequestTime);
                } else {
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

    private void executeArray(DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        int size = inputStream.readInt();
        byte[] data = new byte[size];
        inputStream.readFully(data);
        List<Integer> list = new ArrayList<>(Message.Array.parseFrom(data).getArrayList());

        long startSort = System.nanoTime() / NANOS_IN_MILLIS;
        List<Integer> sortedList = Server.sort(list);
        long endSort = System.nanoTime() / NANOS_IN_MILLIS;

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
        clientThreads.forEach(Thread::interrupt);
        clientThreads.clear();
        serverThread.interrupt();
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
