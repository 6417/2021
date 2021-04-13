package frc.robot.utilities;

import java.util.Optional;
import java.util.function.Supplier;

public class Timer {
    private final Supplier<Long> clock;
    private Optional<Long> lastStarted = Optional.empty();

    public Timer(Supplier<Long> timeMessurmentFunction) {
        clock = timeMessurmentFunction;
    }

    public void start() {
        lastStarted = Optional.of(clock.get());
    }
    
    public Optional<Long> getPastTimeAndRestart() {
        if (lastStarted.isEmpty())
            return Optional.empty();
        long pastTime = clock.get() - lastStarted.get();
        start();
        return Optional.of(pastTime);
    }

    public Optional<Long> getPastTime() {
        if (lastStarted.isEmpty())
            return Optional.empty();
        return Optional.of(clock.get() - lastStarted.get());
    }
}