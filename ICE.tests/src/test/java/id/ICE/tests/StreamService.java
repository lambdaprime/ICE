package id.ICE.tests;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import id.ICE.MessageResponse;
import id.ICE.MessageService;

/*
 * Service which streams given messages to the client without
 * waiting any reply from the client.
 */
class StreamService implements MessageService {
    private final List<String> data;
    private int c = 0;

    public StreamService(List<String> data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<MessageResponse> process(
            ByteBuffer message) {
        var response = new MessageResponse(ByteBuffer.wrap(data.get(c++).getBytes()))
                .withIgnoreNextRequest();
        if (c == data.size())
            response.withCloseOnResponse();
        return CompletableFuture.completedFuture(response);
    }
}