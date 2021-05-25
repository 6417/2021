package ch.fridolins.client;

import javax.swing.*;
import java.awt.*;
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
    private static String robotIP = "10.64.17.2";
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
    }

    private static void putError(String message) {
        frame.getContentPane().setBackground(Color.RED);
        putText(message);
    }

    private static void putText(String message) {
        frame.remove(textLabel);
        frame.validate();
        frame.repaint();
        textLabel.setText("<html><body><div>" + message + "</div></body></html>");
        frame.add(textLabel);
    }

    public static void main(String[] args) {
        initializeJFrame();
        initializeErrorLabel();

        while (true) {
            try {
                putText("Connecting ...");
                Socket socket = new Socket(robotIP, port);
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                BallColor previousBallColor = BallColor.none;
                while (true) {
                    frame.remove(textLabel);
                    frame.validate();
                    frame.repaint();
                    BallColor ballColor = BallColor.none;
                    while (socket.getInputStream().available() > 0) {
                        System.out.println("receiving data");
                    }
                    int receivedChar = reader.read();
                    System.out.println("received: " + Integer.valueOf(Character.toString(receivedChar)));
                    int byteRepresentation = Integer.parseInt(Character.toString(receivedChar));
                    ballColor = BallColor.fromByteRepresentation(byteRepresentation);
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
    }

    private static void initializeJFrame() {
        frame = new JFrame("Ball Color UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setVisible(true);
    }
}
