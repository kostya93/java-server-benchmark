package Server;

import Common.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Server creates a separate thread to communicate by TCP with each client.
 */
public class ServerTcpThreadImpl implements Server {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private List<Thread> clientThreads = new LinkedList<>();


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
                Thread clientThread = new Thread(() -> processClient(clientSocket));
                clientThreads.add(clientThread);
                clientThread.start();
            }
        } catch (Exception ignored) {}
    }
    private void processClient(Socket clientSocket) {
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();

            while (inputStream.read() == 1) {
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
