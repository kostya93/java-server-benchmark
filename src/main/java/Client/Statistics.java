package Client;

/**
 * Created by kostya on 09.12.2016.
 */
public class Statistics {
    private long timePerClientServer;
    private long timePerRequestServer;
    private long timePerClient;

    Statistics() {
    }

    public long getTimePerClientServer() {
        return timePerClientServer;
    }

    void setTimePerClientServer(long timePerClientServer) {
        this.timePerClientServer = timePerClientServer;
    }

    public long getTimePerRequestServer() {
        return timePerRequestServer;
    }

    void setTimePerRequestServer(long timePerRequestServer) {
        this.timePerRequestServer = timePerRequestServer;
    }

    public long getTimePerClient() {
        return timePerClient;
    }

    void setTimePerClient(long timePerClient) {
        this.timePerClient = timePerClient;
    }

    @Override
    public String toString() {
        return String.format("timePerClientServer = %d\ntimePerRequestServer = %d\ntimePerClient = %d",
                timePerClientServer, timePerRequestServer, timePerClient);
    }
}
