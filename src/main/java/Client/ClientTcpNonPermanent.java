package Client;

import Common.*;
import Common.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client make new connection for each request;
 */
class ClientTcpNonPermanent implements Client {
    @Override
    public void run(String host, int port, int N, int delta, int X) throws IOException, InterruptedException {
        for (int i = 0; i < X; i++) {
            try (Socket socket = new Socket(host, port)) {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                outputStream.writeInt(Constants.MessageType.ARRAY);
                Message.Array array = Message.Array.newBuilder().addAllArray(Client.generateList(N)).build();
                int size = array.getSerializedSize();
                outputStream.writeInt(size);
                array.writeTo(outputStream);
                outputStream.flush();
                byte[] data = new byte[size];
                inputStream.readFully(data);
                Message.Array.parseFrom(data).getArrayList();
                outputStream.flush();
            }
            Thread.sleep(delta);
        }
    }
}
