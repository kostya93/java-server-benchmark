package Client;

import Common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import static Common.Constants.MessageType;

/**
 * The client sending data by UDP
 */
class ClientUdp implements Client {
    @Override
    public void run(String host, int port, int N, int delta, int X) throws IOException, InterruptedException {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {

            byte[] data = ByteBuffer.allocate(Integer.BYTES).putInt(MessageType.ARRAY).array();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            datagramSocket.send(packet);
            datagramSocket.receive(packet);
            port = ByteBuffer.wrap(packet.getData()).getInt();

            for (int i = 0; i < X; i++) {
                data = ByteBuffer.allocate(Integer.BYTES).putInt(MessageType.ARRAY).array();
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                datagramSocket.send(datagramPacket);

                Message.Array array = Message.Array.newBuilder().addAllArray(Client.generateList(N)).build();

                int size = array.getSerializedSize();
                data = ByteBuffer.allocate(Integer.BYTES).putInt(size).array();
                datagramPacket.setData(data);
                datagramSocket.send(datagramPacket);

                data = array.toByteArray();
                datagramPacket.setData(data);
                datagramSocket.send(datagramPacket);

                data = new byte[size];
                datagramPacket.setData(data);
                datagramSocket.receive(datagramPacket);
                Message.Array.parseFrom(datagramPacket.getData()).getArrayList();
                Thread.sleep(delta);
            }
            data = ByteBuffer.allocate(Integer.BYTES).putInt(MessageType.END_ARRAYS).array();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            datagramSocket.send(datagramPacket);
        }
    }
}
