package ch.fridolins.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {
    private static enum BallColor {
        blue(1, Color.BLUE), yellow(2, Color.YELLOW), none(0, Color.BLACK);

        public static class InvalidByteRepresentationException extends Exception {
            public InvalidByteRepresentationException(String message) {
                super(message);
            }
        }

        public final int byteRepresentation;
        public final Color jFrameColor;

        private BallColor(int byteRepresentation, Color jFrameColor) {
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
    private static int port = 8080;
    private static String robotIP = "127.0.0.1";
    private static JLabel textLabel;

    private static void setFrameColor(Color color) {
        frame.getContentPane().setBackground(color);
    }

    private static boolean isComponentInFrame(Component component) {
        return Arrays.stream(frame.getContentPane().getComponents()).anyMatch(c -> c.equals(component));
    }

    private static void initializeErrorLabel() {
        textLabel = new JLabel();
        textLabel.setFont(new Font(textLabel.getFont().getName(), Font.BOLD, 48));
        textLabel.setForeground(Color.WHITE);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(textLabel);
    }

    private static void putError(String message) {
        putText(message);
        frame.getContentPane().setBackground(Color.RED);
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        frame.getContentPane().setBackground(Color.DARK_GRAY);
    }

    private static void putText(String message) {
        textLabel.setText("<html><body><div>" + message + "</div></body></html>");
        textLabel.setVisible(true);
    }

    private static boolean mainShouldQuit = false;

    public static void main(String[] args) {
        initializeJFrame();
        initializeErrorLabel();
        Socket socket = null;

        while (!mainShouldQuit) {
            outerMainLoop:
            try {
                putText("Connecting ...");
                socket = new Socket(robotIP, port);
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                BallColor previousBallColor = BallColor.none;
                while (!mainShouldQuit) {
                    textLabel.setVisible(false);
                    BallColor ballColor = BallColor.none;
                    long waitingTimeStart = System.currentTimeMillis();
                    while (socket.getInputStream().available() == 0) {
                        if (System.currentTimeMillis() - waitingTimeStart > 1000) {
                            closeConnection(socket);
                            putError("Disconnected");
                            break outerMainLoop;
                        }
                    }
                    int receivedChar = reader.read();
                    System.out.println("received: " + (int) receivedChar);
                    if (receivedChar == 3) continue;
                    ballColor = BallColor.fromByteRepresentation(receivedChar);
                    if (ballColor != previousBallColor)
                        frame.getContentPane().setBackground(ballColor.jFrameColor);
                    previousBallColor = ballColor;
                }
            } catch (UnknownHostException e) {
                putError("UnknownHostException: " + e.getMessage());
            } catch (IOException e) {
                putError("IOException: " + e.getMessage());
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
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {
                        mainShouldQuit = true;
                    }
                });
    }
}
