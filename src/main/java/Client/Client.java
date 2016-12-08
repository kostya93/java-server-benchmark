package Client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by kostya on 07.12.2016.
 */

interface Client {
    static List<Integer> generateList(int N) {
        Random random = new Random();
        Integer[] arr = new Integer[N];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = random.nextInt();
        }
        return Arrays.asList(arr);
    }

    void run(String host, int port, int N, int delta, int X) throws IOException, InterruptedException;
}
