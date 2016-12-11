package Client;

import Common.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static Common.Constants.MessageType;

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
                Message.Array array = Message.Array.newBuilder().addAllArray(Client.generateList(N)).build();
                int size = array.getSerializedSize();
                outputStream.writeInt(size);
                array.writeTo(outputStream);
                outputStream.flush();
                byte[] data = new byte[size];
                inputStream.readFully(data);
                Message.Array.parseFrom(data).getArrayList();
                Thread.sleep(delta);
            }
            outputStream.writeInt(MessageType.END_ARRAYS);
            outputStream.flush();
        }
    }
}
