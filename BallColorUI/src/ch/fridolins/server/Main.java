package ch.fridolins.server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static enum BallColor {
        blue((byte) 1), yellow((byte) 2), colorNotFound((byte) 0);

        public final byte byteRepresentation;

        private BallColor(byte byteRepresentation) {
            this.byteRepresentation = byteRepresentation;
        }
    }

    public static void main(String[] args) {
//        while (true) {
//            try {
//                BallColor ballColor = BallColor.colorNotFound;
//                System.out.println("initializing ui server.................................");
//                ServerSocket serverSocket = new ServerSocket(8080);
//                Socket clientSocket = serverSocket.accept();
//                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//                while (clientSocket.isConnected()) {
//                    out.print(ballColor);
//                    if ((System.currentTimeMillis() / (int) 1000) % 3 == 0)
//                        ballColor = BallColor.blue;
//                    else if ((System.currentTimeMillis() / (int) 1000) % 4 == 0)
//                        ballColor = BallColor.yellow;
//                    else if ((System.currentTimeMillis() / (int) 1000) % 4 == 0)
//                        ballColor = BallColor.colorNotFound;
//                }
//
//            } catch (Exception e) {
//                System.err.println(e.getMessage());
//            }
//        }
        System.out.println("Hello World");
    }
}
