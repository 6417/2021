package ch.fridolins.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimRioServerDocker {
    public enum BallColor {
        blue(Config.BallColor.blue), yellow(Config.BallColor.yellow), colorNotFound(Config.BallColor.colorNotFound);

        public final byte byteRepresentation;

        BallColor(byte byteRepresentation) {
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
                    Thread.sleep(pingInterval);
                    synchronized (outputStreamMutex) {
                        clientSocket.getOutputStream().write(new byte[]{Config.ping});
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
    private static final long pingInterval = 500; //ms

    public static void main(String[] args) {
        try {
            System.out.println("initializing ui server");
            ServerSocket serverSocket = new ServerSocket(Config.port);
            while (true) {
                BallColor ballColor;
                System.out.println("Waiting for client ...");
                Socket clientSocket = serverSocket.accept();
                Ping ping = new Ping(clientSocket);
                ping.start();
                System.out.println("Client connected");
                BallColor previousBallColor = null;
                while (true) {
                    ballColor = updateBallColor();
                    if (!sendDataToClient(ballColor, clientSocket, previousBallColor)) break;
                    previousBallColor = ballColor;
                }
                System.out.println("Client disconnected");
                disconnectToClient(clientSocket, ping);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void disconnectToClient(Socket clientSocket, Ping ping) throws IOException {
        ping.interrupt();
        clientSocket.close();
    }

    private static BallColor updateBallColor() {
        BallColor ballColor;
        if ((System.currentTimeMillis() / 1000) % 3 == 0)
            ballColor = BallColor.blue;
        else if ((System.currentTimeMillis() / 1000) % 2 == 0)
            ballColor = BallColor.yellow;
        else
            ballColor = BallColor.colorNotFound;
        return ballColor;
    }

    /**
     * @return False if the connection to the client was lost, true if connection is ok
     */
    private static boolean sendDataToClient(BallColor ballColor, Socket clientSocket, BallColor previousBallColor) throws IOException {
        if (ballColor != previousBallColor)
            synchronized (outputStreamMutex) {
                try {
                    clientSocket.getOutputStream().write(new byte[]{ballColor.byteRepresentation});
                } catch (SocketException e) {
                    return false;
                }
            }
        return true;
    }
}
