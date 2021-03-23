package frc.robot.commands.Swerve;

import java.util.Map;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveDrive;
import frc.robot.subsystems.Swerve.SwerveModule;

public class ZeroEncoders extends SequentialCommandGroup {
    public ZeroEncoders() {
        addRequirements(SwerveDrive.getInstance());
        addCommands(new GoToHalsensor(), new GoToZeroPosition());
    }

    private class GoToHalsensor extends CommandBase {
        @Override
        public void initialize() {
            SwerveDrive.getInstance().forEachModule((module) -> module.setEncoderZeroedFalse());
            SwerveDrive.getInstance().stopAllMotors();
            SwerveDrive.getInstance().rotateAllModules(Constants.SwerveDrive.zeroingSpeed);
            SwerveDrive.getInstance().forEachModule((module) -> module.enableLimitSwitch());
        }

        @Override
        public void execute() {
            for (var isHalSensorTriggeredOfMoudle : SwerveDrive.getInstance().areHalSensoredOfMoudlesTriggered()
                    .entrySet())
                if (isHalSensorTriggeredOfMoudle.getValue()
                        && !SwerveDrive.getInstance().isModuleZeroed(isHalSensorTriggeredOfMoudle.getKey()))
                    SwerveDrive.getInstance().setCurrentModuleRotatoinToHome(isHalSensorTriggeredOfMoudle.getKey());

            SwerveDrive.getInstance().forEachModule((module) -> {
                if (module.hasEncoderBeenZeroed())
                    module.stopAllMotors();
            });
        }

        @Override
        public boolean isFinished() {
            return SwerveDrive.getInstance().areAllModulesZeroed();
        }

        @Override
        public void end(boolean interrupted) {
            SwerveDrive.getInstance().stopAllMotors();
            SwerveDrive.getInstance().forEachModule((module) -> module.disableLimitSwitch());
        }
    }

    private class GoToZeroPosition extends CommandBase {
        boolean allModulesAtSetpoint;

        @Override
        public void initialize() {
            SwerveDrive.getInstance()
                    .forEachModuleEntry((Map.Entry<Constants.SwerveDrive.MountingLocations, SwerveModule> entry) -> {
                        entry.getValue().setDesiredState(
                                Constants.SwerveDrive.swerveModuleConfigs.get(entry.getKey()).homeState);
                        entry.getValue().drive();
                    });
            SwerveDrive.getInstance().forEachModule((module) -> module.setEncoderZeroedFalse());
        }

        @Override
        public void execute() {
            SwerveDrive.getInstance().forEachModule((module) -> {
                if (Math.acos(module.getTargetVector().dot(module.getModuleRotation())) < 0.15 && !module.hasEncoderBeenZeroed()) {
                    module.setCurrentRotationToEncoderHome();
                    module.stopAllMotors();
                }
            });
        }

        @Override
        public boolean isFinished() {
            // allModulesAtSetpoint = true;
            // SwerveDrive.getInstance().forEachModule((module) -> allModulesAtSetpoint &= Math
            //         .acos(module.getTargetVector().dot(module.getModuleRotation())) < 0.15);
            // return allModulesAtSetpoint;
            return SwerveDrive.getInstance().areAllModulesZeroed();
        }
    }
}