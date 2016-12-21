package rsc.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;

import org.reactivestreams.Subscriber;

import rsc.documentation.Operator;
import rsc.documentation.OperatorType;
import rsc.flow.Disposable;
import rsc.flow.Receiver;

/**
 * Connects to the underlying ConnectablePublisher once the given amount of Subscribers
 * subscribed.
 *
 * @param <T> the value type
 */
@Operator(traits = OperatorType.CONNECTABLE, aliases = {"autoConnect"})
public final class ConnectablePublisherAutoConnect<T> extends Px<T>
        implements Receiver {

    final ConnectablePublisher<? extends T> source;

    final Consumer<? super Disposable> cancelSupport;

    volatile int remaining;
    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<ConnectablePublisherAutoConnect> REMAINING =
            AtomicIntegerFieldUpdater.newUpdater(ConnectablePublisherAutoConnect.class, "remaining");


    public ConnectablePublisherAutoConnect(ConnectablePublisher<? extends T> source, 
            int n, Consumer<? super Disposable> cancelSupport) {
        if (n <= 0) {
            throw new IllegalArgumentException("n > required but it was " + n);
        }
        this.source = Objects.requireNonNull(source, "source");
        this.cancelSupport = Objects.requireNonNull(cancelSupport, "cancelSupport");
        REMAINING.lazySet(this, n);
    }
    
    @Override
    public void subscribe(Subscriber<? super T> s) {
        source.subscribe(s);
        if (remaining > 0 && REMAINING.decrementAndGet(this) == 0) {
            source.connect(cancelSupport);
        }
    }

    @Override
    public Object upstream() {
        return source;
    }
}
