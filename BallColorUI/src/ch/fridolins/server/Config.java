package ch.fridolins.server;

// All changes also have to be applied in the ${PROJECT_ROOT}\src\main\java\ch\fridolins\server\Config.java file

public class Config {
    public static class BallColor {
        public static final byte blue = 1;
        public static final byte yellow = 2;
        public static final byte colorNotFound = 0;
    }

    public static final char ping = 3;
    public static final int port = 8080;
}
