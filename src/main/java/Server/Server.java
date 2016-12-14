package Server;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by kostya on 07.12.2016.
 */

public interface Server {
    void start(int port) throws IOException;

    void stop() throws IOException;

    void reset() throws IOException;

    static List<Integer> sort(List<Integer> input) {
        for (int i = input.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (input.get(j) > input.get(j + 1)) {
                    Collections.swap(input, j, j + 1);
                }
            }
        }
        return input;
    }

    long getTimeForClients();
    long getTimeForRequests();
}
