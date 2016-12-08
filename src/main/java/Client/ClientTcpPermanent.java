package Client;

import Common.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The client establishes a permanent connection for all requests
 */

class ClientTcpPermanent implements Client {
    @Override
    public void run(String host, int port, int N, int delta, int X) throws IOException, InterruptedException {
        try (Socket socket = new Socket(host, port)) {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            for (int i = 0; i < X; i++) {
                outputStream.write(StreamDelimiter.START_MESSAGE);
                Message.Array
                        .newBuilder()
                        .addAllArray(Client.generateList(N))
                        .build()
                        .writeDelimitedTo(outputStream);
                Message.Array.parseDelimitedFrom(inputStream).getArrayList();
                outputStream.flush();
                Thread.sleep(delta);
            }
            outputStream.write(StreamDelimiter.END_STREAM);
            outputStream.flush();
        }
    }
}
