package Server;

import Common.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kostya on 13.12.2016.
 */

public class ServerRunner {
    public static void run(int configurePort) throws IOException {
        Server server = null;
        int serverType = 0;
        int processPort = 0;
        try (ServerSocket serverSocket = new ServerSocket(configurePort)) {
            try (Socket client = serverSocket.accept()) {
                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                int configMessage;
                while (true) {
                    configMessage = dataInputStream.readInt();
                    switch (configMessage) {
                        case Constants.ConfigureMessage.START_SERVER:
                            serverType = dataInputStream.readInt();
                            processPort = dataInputStream.readInt();
                            server = getServer(serverType);
                            server.start(processPort);
                            dataOutputStream.writeBoolean(true);
                            dataOutputStream.flush();
                            break;
                        case Constants.ConfigureMessage.STOP_SERVER:
                            if (server != null) {
                                server.stop();
                            }
                            dataOutputStream.writeBoolean(true);
                            dataOutputStream.flush();
                            break;
                        case Constants.ConfigureMessage.RESET_SERVER:
                            if (server != null) {
                                server.stop();
                                server.start(processPort);
                            }
                            dataOutputStream.writeBoolean(true);
                            dataOutputStream.flush();
                            break;
                        case Constants.ConfigureMessage.STATS:
                            if (server != null) {
                                dataOutputStream.writeLong(server.getTimeForClients());
                                dataOutputStream.writeLong(server.getTimeForRequests());
                            } else {
                                dataOutputStream.writeLong(-1L);
                                dataOutputStream.writeLong(-1L);
                            }
                            dataOutputStream.flush();
                            break;
                        case Constants.ConfigureMessage.EXIT:
                            if (server != null) {
                                server.stop();
                            }
                            return;
                        default:
                            throw new NotImplementedException();
                    }
                }
            }
        }
    }

    private static Server getServer(int serverType) {
        switch (serverType) {
            case Constants.ServerType.TCP_ASYNC:
                return new ServerTcpAsync();
            case Constants.ServerType.TCP_CACHED_THREAD_POOL:
                return new ServerTcpCachedThreadPool();
            case Constants.ServerType.TCP_NON_BLOCKING:
                return new ServerTcpNonBlocking();
            case Constants.ServerType.TCP_ONE_THREAD_SEQUENTIAL:
                return new ServerTcpOneThreadSequential();
            case Constants.ServerType.TCP_THREAD:
                return new ServerTcpThread();
            case Constants.ServerType.UDP_THREAD:
                return new ServerUdpThread();
            case Constants.ServerType.UDP_TREAD_POOL:
                return new ServerUdpThreadPool();
            default:
                throw new NotImplementedException();
        }
    }
}
