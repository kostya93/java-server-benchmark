import Client.ClientRunner;

import java.io.IOException;

import static Client.Constants.ClientType;

/**
 * Created by kostya on 07.12.2016.
 */
public class ClientCli {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClientRunner clientRunner = new ClientRunner("localhost", 55555, 10, 3, 1000, 5, ClientType.TCP_PERMANENT);
        System.out.println(clientRunner.run());
    }
}
