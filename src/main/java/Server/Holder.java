package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static Common.Constants.NANOS_IN_MILLIS;

/**
 * Created by kostya on 09.12.2016.
 */

class Holder {
    private ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
    private State state = State.READING_TYPE;
    private long timeStartRequest;

    enum State {
        READING_TYPE,
        READING_SIZE,
        READING_ARRAY,
        END_READING_TYPE,
        END_READING_SIZE,
        END_READING_ARRAY,
        WRITING_ARRAY,
        WRITING_STATS,
        END_WRITING_ARRAY,
        END_WRITING_STATS;
    }

    int read(SocketChannel client) throws IOException {
        int read = client.read(byteBuffer);
        if (read > 0 && isBufferFull()) {
            switch (state) {
                case READING_TYPE:
                    state = State.END_READING_TYPE;
                    break;
                case READING_SIZE:
                    state = State.END_READING_SIZE;
                    break;
                case READING_ARRAY:
                    state = State.END_READING_ARRAY;
                    break;
            }
        }
        return read;
    }

    int write(SocketChannel client) throws IOException {
        int write = client.write(byteBuffer);
        if (write > 0 && isBufferFull()) {
            switch (state) {
                case WRITING_ARRAY:
                    state = State.END_WRITING_ARRAY;
                    break;
                case WRITING_STATS:
                    state = State.END_WRITING_STATS;
                    break;
            }
        }
        return write;
    }

    long getTimeStartRequest() {
        return timeStartRequest;
    }

    void setTimeStartRequest(long timeStartRequest) {
        this.timeStartRequest = timeStartRequest;
    }

    State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    void createBuffer(int capacity) {
        byteBuffer = ByteBuffer.allocate(capacity);
    }

    private boolean isBufferFull() {
        return byteBuffer.limit() == byteBuffer.position();
    }

    boolean isReading() {
        return state == State.READING_TYPE || state == State.READING_ARRAY || state == State.READING_SIZE;
    }

    boolean isStartRequest() {
        return state == State.READING_TYPE && byteBuffer.position() == 0;
    }

    boolean isWriting() {
        return state == State.WRITING_ARRAY || state == State.WRITING_STATS;
    }
}
