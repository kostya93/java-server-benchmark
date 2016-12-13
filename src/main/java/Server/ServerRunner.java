package Server;

import Common.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kostya on 13.12.2016.
 */

public class ServerRunner {
    public static void run(int port) throws IOException {
        int serverType;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (Socket client = serverSocket.accept()) {
                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                serverType = dataInputStream.readInt();
            }
        }
        switch (serverType) {
            case Constants.ServerType.TCP_ASYNC:
                new ServerTcpAsync().start(port);
                break;
            case Constants.ServerType.TCP_CACHED_THREAD_POOL:
                new ServerTcpCachedThreadPool().start(port);
                break;
            case Constants.ServerType.TCP_NON_BLOCKING:
                new ServerTcpNonBlocking().start(port);
                break;
            case Constants.ServerType.TCP_ONE_THREAD_SEQUENTIAL:
                new ServerTcpOneThreadSequential().start(port);
                break;
            case Constants.ServerType.TCP_THREAD:
                new ServerTcpThread().start(port);
                break;
            case Constants.ServerType.UDP_THREAD:
                new ServerUdpThread().start(port);
                break;
            case Constants.ServerType.UDP_TREAD_POOL:
                new ServerUdpThreadPool().start(port);
                break;
            default:
                throw new NotImplementedException();
        }
    }
}
