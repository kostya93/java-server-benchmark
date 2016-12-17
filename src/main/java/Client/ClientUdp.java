package Client;

import Common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static Common.Constants.MessageType;

/**
 * The client sending data by UDP
 */
class ClientUdp implements Client {
    @Override
    public void run(String host, int port, int N, int delta, int X) throws IOException, InterruptedException {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            for (int i = 0; i < X; i++) {
                Message.Array array = Message.Array.newBuilder().addAllArray(Client.generateList(N)).build();
                byte[] data = array.toByteArray();
                DatagramPacket request = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                datagramSocket.send(request);
                DatagramPacket response = new DatagramPacket(new byte[data.length], data.length);
                datagramSocket.receive(response);
                Message.Array.parseFrom(response.getData()).getArrayList();
                Thread.sleep(delta);
            }
        }
    }
}
