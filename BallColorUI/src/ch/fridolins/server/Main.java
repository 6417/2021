package ch.fridolins.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static enum BallColor {
        blue((byte) 1), yellow((byte) 2), colorNotFound((byte) 0);

        public final byte byteRepresentation;

        private BallColor(byte byteRepresentation) {
            this.byteRepresentation = byteRepresentation;
        }
    }


    static class Ping implements Runnable {
        private Socket clientSocket;
        private Thread worker;

        public Ping(Socket clientSocket) {
            this.clientSocket = clientSocket;
            worker = new Thread(this);
        }

        private static final AtomicBoolean pingLoopRunning = new AtomicBoolean(true);

        @Override
        public void run() {
            pingLoopRunning.set(true);
            while (pingLoopRunning.get()) {
                try {
                    Thread.sleep(500);
                    synchronized (outputStreamMutex) {
                        clientSocket.getOutputStream().write(new byte[]{pingChar});
                    }
                } catch (SocketException e) {
                    pingLoopRunning.set(false);
                    System.out.println("Thread shutting down");
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        public void start() {
            worker.start();
        }

        public void interrupt() {
            pingLoopRunning.set(false);
            worker.interrupt();
        }
    }

    private static final Object outputStreamMutex = new Object();
    private static final char pingChar = 3;

    public static void main(String[] args) {
        try {
            System.out.println("initializing ui server ...");
            ServerSocket serverSocket = new ServerSocket(8080);
            Ping ping;
            while (true) {
                BallColor ballColor = BallColor.colorNotFound;
                System.out.println("Waiting for client ...");
                Socket clientSocket = serverSocket.accept();
                ping = new Ping(clientSocket);
                ping.start();
                System.out.println("Client connected!!!");
                BallColor previousBallColor = BallColor.colorNotFound;
                while (true) {
                    if ((System.currentTimeMillis() / 1000) % 3 == 0)
                        ballColor = BallColor.blue;
                    else if ((System.currentTimeMillis() / (int) 1000) % 2 == 0)
                        ballColor = BallColor.yellow;
                    else
                        ballColor = BallColor.colorNotFound;
                    if (ballColor != previousBallColor) {
                        synchronized (outputStreamMutex) {
                            try {
                                clientSocket.getOutputStream().write(new byte[]{ballColor.byteRepresentation});
                            } catch (SocketException e) {
                                break;
                            }
                        }
                    }
                    previousBallColor = ballColor;
                }
                System.out.println("Client disconnected");
                ping.interrupt();
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
