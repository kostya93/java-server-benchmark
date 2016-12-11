import Server.*;

import java.io.IOException;

/**
 * Created by kostya on 08.12.2016.
 */
public class ServerCli {
    public static void main(String[] args) throws IOException {
        Server server = new ServerUdpThreadPool();
        server.start(55555);
    }
}
