package frc.robot.commands.Swerve;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveDrive;
import frc.robot.subsystems.Swerve.SwerveModule;

public class ZeroEncoders extends ParallelCommandGroup {
    public ZeroEncoders() {
        addRequirements(SwerveDrive.getInstance());
        List<SequentialCommandGroup> commands = new ArrayList<>();
        SwerveDrive.getInstance().forEachModule((module) -> commands.add(new SequentialCommandGroup(new GoToHalSensor(module), new Finetune(module))));
        addCommands(commands.toArray(SequentialCommandGroup[]::new));
    }

    private class GoToHalSensor extends CommandBase {
        private double currentSetPoint;
        private SwerveModule module;

        public GoToHalSensor(SwerveModule module) {
            this.module = module;
        }

        @Override
        public void initialize() {
            module.setEncoderZeroedFalse();
            module.stopAllMotors();
            module.enableLimitSwitch();
            currentSetPoint = module.getRotationEncoderTicks() + Constants.SwerveDrive.zeroingSpeed;
        }

        @Override
        public void execute() {
            module.setDesiredRotationMotorTicks(currentSetPoint);
            currentSetPoint += Constants.SwerveDrive.zeroingSpeed;
        }

        @Override
        public void end(boolean interrupted) {
            module.stopAllMotors();
        }

        @Override
        public boolean isFinished() {
            return module.isHalSensorTriggered();
        }
    }

    private class Finetune extends CommandBase {
        private double currentSetPoint;
        private SwerveModule module;

        public Finetune(SwerveModule module) {
            this.module = module;
        }

        @Override
        public void initialize() {
            module.setEncoderZeroedFalse();
            module.stopAllMotors();
            module.enableLimitSwitch();
            currentSetPoint = module.getRotationEncoderTicks() - Constants.SwerveDrive.zeroingSpeed * Constants.SwerveDrive.finetuningZeroFactor;
            while (module.isHalSensorTriggered()) {
                module.setDesiredRotationMotorTicks(currentSetPoint);
                currentSetPoint -= Constants.SwerveDrive.zeroingSpeed * Constants.SwerveDrive.finetuningZeroFactor;
            }
            currentSetPoint = module.getRotationEncoderTicks();
        }

        @Override
        public void execute() {
            module.setDesiredRotationMotorTicks(currentSetPoint);
            currentSetPoint += Constants.SwerveDrive.zeroingSpeed * Constants.SwerveDrive.finetuningZeroFactor;
            if (module.isHalSensorTriggered())
                module.setRotationEncoderTicks(module.halSensorPosition);
        }

        @Override
        public boolean isFinished() {
            return module.hasEncoderBeenZeroed();
        }

        @Override
        public void end(boolean interrupted) {
            module.stopAllMotors();
            module.disableLimitSwitch();
        }
    }
}