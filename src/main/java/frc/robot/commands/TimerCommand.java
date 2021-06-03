package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;

public class TimerCommand extends CommandBase{
    private double duration;
    private long startTime;

    public TimerCommand(double duration) {
        this.duration = duration;
    }

    @Override
    public void initialize() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isFinished() {
        return (System.currentTimeMillis() - startTime) / 1000.0 >= duration;
    }
    
}
