package id.ICE.tests;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import id.ICE.MessageResponse;
import id.ICE.MessageService;
import id.xfunction.concurrent.DelayedCompletableFuture;

/**
 * Service which accumulates all received data into internal collection
 */
class AccumulatorService implements MessageService {
    Collection<String> received = new ConcurrentLinkedQueue<>();

    @Override
    public CompletableFuture<MessageResponse> process(ByteBuffer req) {
        var message = new String(req.array());
        received.add(message);
        System.out.format("%d => %s\n", received.size(), message);
        return new DelayedCompletableFuture<>(null, 10, 50);
    }
}