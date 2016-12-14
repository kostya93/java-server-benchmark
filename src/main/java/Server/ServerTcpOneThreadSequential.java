package Server;

import Common.Constants;
import Common.Message;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static Common.Constants.NANOS_IN_MILLIS;

/**
 * The server performs a single-threaded sequential processing.
 * Requests are processed sequentially with
 * closing connection after sending response.
 */
class ServerTcpOneThreadSequential implements Server {
    private ServerSocket serverSocket;
    private Thread serverThread;

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
                processClient(clientSocket);
                clientSocket.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void processClient(Socket clientSocket) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

        int messageType = inputStream.readInt();
        if (messageType == Constants.MessageType.ARRAY) {
            long startRequestTime = System.nanoTime() / NANOS_IN_MILLIS;
            executeArray(inputStream, outputStream);
            long endRequestTime = System.nanoTime() / NANOS_IN_MILLIS;
            timeForRequests.add(endRequestTime - startRequestTime);
        } else {
            throw new NotImplementedException();
        }
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

        serverThread.interrupt();
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
