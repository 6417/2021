package frc.robot.utilities.baseClasses;

import frc.robot.utilities.VisionService;
import frc.robot.utilities.VisionService.Values;

public class VisionServiceBase {
    public VisionServiceBase() {
    }

    public Values getValues() {
        return new VisionService.Values();
    }

    public void setConnectionStatus() {
    }
}