package uk.ac.dur;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.stream.Stream;

public class Utils {
    public static final Random RANDOM = new Random();

    @NotNull
    public static String generateID() {
        return Stream.generate(() ->
                Integer.toString(RANDOM.nextInt(36), 36).toUpperCase()
        ).limit(10).reduce(String::concat).orElse("");
    }
}
