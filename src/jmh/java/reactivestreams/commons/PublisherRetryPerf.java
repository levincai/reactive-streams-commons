package reactivestreams.commons;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import reactivestreams.commons.internal.PerfSubscriber;


/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='PublisherRetryPerf'
 * 
 *
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class PublisherRetryPerf {

    @Param({"1", "1000", "1000000"})
    int count;
    
    Integer[] array;
    
    Publisher<Integer> source;
    
    PerfSubscriber sharedSubscriber;
    
    @Setup
    public void setup(Blackhole bh) {
        array = new Integer[count];
        for (int i = 0; i < count; i++) {
            array[i] = 777;
        }
        source = createSource();
        sharedSubscriber = new PerfSubscriber(bh);
    }
    
    Publisher<Integer> createSource() {
        return new PublisherRetry<>(new PublisherArray<>(array));
    }
    
    @Benchmark
    public void standard(Blackhole bh) {
        source.subscribe(new PerfSubscriber(bh));
    }
    
    @Benchmark
    public void shared() {
        source.subscribe(sharedSubscriber);
    }
    
    @Benchmark
    public void createNew(Blackhole bh) {
        Publisher<Integer> p = createSource();
        bh.consume(p);
        p.subscribe(new PerfSubscriber(bh));
    }
}
