package ch.fridolins.client;

import ch.fridolins.server.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
    public static final int errorDelayBeforeStartup = 1000;

    private static enum BallColor {
        blue(Config.BallColor.blue, Color.BLUE), yellow(Config.BallColor.yellow, Color.YELLOW), none(Config.BallColor.colorNotFound, Color.BLACK);

        public static class InvalidByteRepresentationException extends Exception {
            public InvalidByteRepresentationException(String message) {
                super(message);
            }
        }

        public final byte byteRepresentation;
        public final Color jFrameColor;

        private BallColor(byte byteRepresentation, Color jFrameColor) {
            this.byteRepresentation = byteRepresentation;
            this.jFrameColor = jFrameColor;
        }

        public static BallColor fromByteRepresentation(int byteRepresentation) throws InvalidByteRepresentationException {
            for (var v : values())
                if (v.byteRepresentation == byteRepresentation)
                    return v;
            throw new InvalidByteRepresentationException("invalid byte Representation");
        }
    }

    private static JFrame frame;
    private static String robotIP = "10.64.17.2";
    private static JLabel textLabel;
    public static final int maxTimeAmountToWaitOnServer = 3000;
    public static final int errorShowTime = 25000;

    private static void initializeTextLabel() {
        textLabel = new JLabel();
        textLabel.setFont(new Font(textLabel.getFont().getName(), Font.BOLD, 48));
        textLabel.setForeground(Color.WHITE);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(textLabel);
    }

    private static Thread removeErrorMessageThread = new Thread(() -> {
        try {
            Thread.sleep(errorShowTime);
        } catch (InterruptedException e) {
            return;
        }
        textLabel.setVisible(false);
    });

    private static void putError(String message) {
        putText(message);
        System.err.println("Error: " + message);
        setWindowColor(Color.RED);
        restartRemoveErrorMessageThread();
    }

    private static void restartRemoveErrorMessageThread() {
        try {
            removeErrorMessageThread.start();
        } catch (IllegalThreadStateException e) {
            killRemoveErrorMessageThread();
            removeErrorMessageThread.start();
        }
    }

    private static void killRemoveErrorMessageThread() {
        removeErrorMessageThread.interrupt();
        removeErrorMessageThread = new Thread(() -> {
            try {
                Thread.sleep(errorShowTime);
            } catch (InterruptedException interrupt) {
                return;
            }
            textLabel.setVisible(false);
        });
    }

    private static void putText(String message) {
        textLabel.setText("<html><body><div>" + message + "</div></body></html>");
        textLabel.setVisible(true);
    }

    private static boolean mainShouldQuit = false;
    private static Socket socket = null;

    public static void main(String[] args) {
        initializeJFrame();
        initializeTextLabel();
        putText("Connecting ...");
        long startupTime = System.currentTimeMillis();

        while (!mainShouldQuit) {
            CONNECT_TO_SERVER:
            try {
                socket = new Socket(robotIP, Config.port);
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                BallColor previousBallColor = BallColor.none;
                killRemoveErrorMessageThread();
                textLabel.setVisible(false);
                setWindowColor(BallColor.none.jFrameColor);
                while (!mainShouldQuit) {
                    BallColor ballColor = BallColor.none;
                    long waitingTimeStart = System.currentTimeMillis();
                    while (socket.getInputStream().available() == 0) {
                        if (System.currentTimeMillis() - waitingTimeStart > maxTimeAmountToWaitOnServer) {
                            closeConnection(socket);
                            putError("Disconnected");
                            break CONNECT_TO_SERVER;
                        }
                    }
                    int receivedChar = reader.read();
                    System.out.println("Received: " + receivedChar + ", ballColor char: " + previousBallColor.byteRepresentation);
                    if (receivedChar == Config.ping) continue;
                    ballColor = BallColor.fromByteRepresentation(receivedChar);
                    if (ballColor != previousBallColor)
                        setWindowColor(ballColor.jFrameColor);
                    previousBallColor = ballColor;
                }
            } catch (UnknownHostException e) {
                putError("UnknownHostException: " + e.getMessage());
            } catch (IOException e) {
                if (System.currentTimeMillis() - startupTime > errorDelayBeforeStartup)
                    putError("<p>Connecting ... <br>Are you connected to the robot, and is it on?</p><p style=\"font-size:16\"><br><br>(IOException: " + e.getMessage() + ")</p>");
            } catch (BallColor.InvalidByteRepresentationException e) {
                putError("InvalidByteRepresentationException: " + e.getMessage());
            }
        }
        if (socket != null) {
            try {
                System.out.println("shutting down ...");
                closeConnection(socket);
            } catch (IOException e) {
                System.err.println("Error while shutting down: " + e.getMessage());
            }
        }
    }

    private static void closeConnection(Socket socket) throws IOException {
        socket.getOutputStream().write(new byte[]{(byte) 0xf});
        socket.close();
    }

    private static void initializeJFrame() {
        frame = new JFrame("Ball Color UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setWindowColor(Color.BLACK);
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {
                        mainShouldQuit = true;
                    }
                });
    }

    private static void setWindowColor(Color color) {
        frame.getContentPane().setBackground(color);
        System.out.println("color set to: " + color.toString());
    }
}
