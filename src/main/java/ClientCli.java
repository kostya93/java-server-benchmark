import Client.ClientRunner;

import java.io.IOException;

import static Client.Constants.ClientType;
import static Common.Constants.ServerType;

/**
 * Created by kostya on 07.12.2016.
 */
public class ClientCli {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClientRunner clientRunner = new ClientRunner("localhost", 55555, ClientType.UDP);
        clientRunner.configureServer(ServerType.UDP_THREAD);
        for (int i = 1; i < 10; i++) {
            System.out.println(clientRunner.run(1000, i, 200, 5));
        }
    }
}
