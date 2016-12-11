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

/**
 * The server performs a single-threaded sequential processing.
 * Requests are processed sequentially with
 * closing connection after sending response.
 */
public class ServerTcpOneThreadSequential implements Server {
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
        switch (messageType) {
            case Constants.MessageType.ARRAY:
                executeArray(inputStream, outputStream);
                break;
            case Constants.MessageType.STATS:
                executeStats(outputStream);
                break;
            default:
                throw new NotImplementedException();
        }
    }

    private void executeStats(DataOutputStream outputStream) throws IOException {
        outputStream.writeLong(timeForClients.longValue());
        outputStream.writeLong(timeForRequests.longValue());
        outputStream.flush();
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

        serverThread.interrupt();
        serverSocket.close();
        serverSocket = null;
    }
}
