package id.ICE;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class DelayedCompletableFuture<T> extends CompletableFuture<T> {

    /**
     * @param millis number of milliseconds when future completes
     */
    public DelayedCompletableFuture(T value, long millis) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            complete(value);
        });
    }

    /**
     * Completes a future with a random delay between [startMillis, endMillis)
     */
    public DelayedCompletableFuture(T value, long startMillis, long endMillis) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                
                Thread.sleep((long)(startMillis + (endMillis - startMillis) * Math.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            complete(value);
        });
    }

}
