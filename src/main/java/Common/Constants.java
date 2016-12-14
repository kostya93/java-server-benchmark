package Common;

/**
 * Created by kostya on 09.12.2016.
 */
public class Constants {
    public static class MessageType {
        public final static int ARRAY = 1;
        public final static int STATS = 2;
        public final static int END_ARRAYS = 3;
    }

    public static class ConfigureMessage {
        public final static int START_SERVER = 1;
        public final static int STOP_SERVER = 2;
        public final static int RESET_SERVER = 3;
        public final static int STATS = 4;
        public final static int EXIT = 5;
    }

    public static class ServerType {
        public final static int TCP_ASYNC = 1;
        public final static int TCP_CACHED_THREAD_POOL = 2;
        public final static int TCP_NON_BLOCKING = 3;
        public final static int TCP_ONE_THREAD_SEQUENTIAL = 4;
        public final static int TCP_THREAD = 5;
        public final static int UDP_THREAD = 6;
        public final static int UDP_TREAD_POOL = 7;
    }

    public final static int NANOS_IN_MILLIS = 1_000_000;
}
