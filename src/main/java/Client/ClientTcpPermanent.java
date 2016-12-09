package Client;

import Common.*;

import java.io.*;
import java.net.Socket;

import static Common.Constants.*;

/**
 * The client establishes a permanent connection for all requests
 */

class ClientTcpPermanent implements Client {
    @Override
    public void run(String host, int port, int N, int delta, int X) throws IOException, InterruptedException {
        try (Socket socket = new Socket(host, port)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            for (int i = 0; i < X; i++) {
                outputStream.writeInt(MessageType.ARRAY);
                Message.Array
                        .newBuilder()
                        .addAllArray(Client.generateList(N))
                        .build()
                        .writeDelimitedTo(outputStream);
                Message.Array.parseDelimitedFrom(inputStream).getArrayList();
                outputStream.flush();
                Thread.sleep(delta);
            }
            outputStream.writeInt(MessageType.END);
            outputStream.flush();
        }
    }
}
