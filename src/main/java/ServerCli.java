import Server.*;

import java.io.IOException;

/**
 * Created by kostya on 08.12.2016.
 */
public class ServerCli {
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("new client");
            ServerRunner.run(44444);
        }
    }
}
