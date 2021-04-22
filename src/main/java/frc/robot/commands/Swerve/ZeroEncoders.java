package frc.robot.commands.swerve;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.Controller;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.swerve.SwerveModule;

public class ZeroEncoders extends ParallelCommandGroup {
    public ZeroEncoders() {
        addRequirements(SwerveDrive.getInstance());
        List<SequentialCommandGroup> commands = new ArrayList<>();
        SwerveDrive.getInstance().forEachModule((module) -> {
            SequentialCommandGroup commandGroup = new SequentialCommandGroup(new GoToHalSensor(module));
            commandGroup.addCommands(new Finetune(module, commandGroup));
            commands.add(commandGroup);
        });
        addCommands(commands.toArray(SequentialCommandGroup[]::new));
        addCommands(new InstantCommand(
                () -> Controller.getInstance().driveJoystick.setRumble(RumbleType.kLeftRumble, 1.0)));
        addCommands(new InstantCommand(
                () -> Controller.getInstance().driveJoystick.setRumble(RumbleType.kRightRumble, 1.0)));
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        Controller.getInstance().driveJoystick.setRumble(RumbleType.kLeftRumble, 0.0);
        Controller.getInstance().driveJoystick.setRumble(RumbleType.kRightRumble, 0.0);
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
        private double startingPosition;
        private CommandBase parentSequentialCommand;

        public Finetune(SwerveModule module, CommandBase parentSequentialCommand) {
            this.module = module;
            this.parentSequentialCommand = parentSequentialCommand;
        }

        @Override
        public void initialize() {
            module.setEncoderZeroedFalse();
            module.stopAllMotors();
            module.enableLimitSwitch();
            currentSetPoint = module.getRotationEncoderTicks()
                    - Constants.SwerveDrive.zeroingSpeed * Constants.SwerveDrive.finetuningZeroFactor;
            while (module.isHalSensorTriggered()) {
                module.setDesiredRotationMotorTicks(currentSetPoint);
                currentSetPoint -= Constants.SwerveDrive.zeroingSpeed * Constants.SwerveDrive.finetuningZeroFactor;
            }
            currentSetPoint = module.getRotationEncoderTicks();
            startingPosition = module.getRotationEncoderTicks();
        }

        @Override
        public void execute() {
            module.setDesiredRotationMotorTicks(currentSetPoint);
            currentSetPoint += Constants.SwerveDrive.zeroingSpeed * Constants.SwerveDrive.finetuningZeroFactor;
            if (module.isHalSensorTriggered())
                module.setRotationEncoderTicks(module.halSensorPosition);

            // TODO: Debug this if statement
            // if (Math.abs(module.getRotationEncoderTicks() - startingPosition) >
            // Constants.SwerveDrive.maxFineTuneOffsetForZeroEncodersCommand) {
            // CommandScheduler.getInstance().cancel(parentSequentialCommand);
            // SequentialCommandGroup newZeroCommandForModule = new
            // SequentialCommandGroup(new GoToHalSensor(module));
            // newZeroCommandForModule.addCommands(new Finetune(module,
            // newZeroCommandForModule));
            // CommandScheduler.getInstance().schedule(newZeroCommandForModule);
            // }
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