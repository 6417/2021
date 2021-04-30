package frc.robot.utilities;

import java.util.Stack;

public class MovingAverageFilter {
    private int historySize;
    private Stack<Double> history;

    public MovingAverageFilter(int historySize) {
        history = new Stack<>();
        this.historySize = historySize;
    }

    private void pushToHistory(double value) {
        while (history.size() >= historySize) {
            history.remove(0);
        }
        history.push(value);
    }

    public double calculate(double value) {
        pushToHistory(value);    
        return history.stream().reduce(0.0, (v1, v2) -> v1 + v2) / history.size();
    }
}