package arr.armuriii.spiritum.block.entity;

import net.minecraft.util.math.MathHelper;

public class PedestalClientData {
    public static final float DISPLAY_ROTATION_SPEED = 360f/20/16;
    private float displayRotation;
    private float prevDisplayRotation;

    PedestalClientData() {
    }

    public float getDisplayRotation() {
        return this.displayRotation;
    }

    public float getPreviousDisplayRotation() {
        return this.prevDisplayRotation;
    }

    void rotateDisplay() {
        this.prevDisplayRotation = this.displayRotation;
        this.displayRotation = MathHelper.wrapDegrees(this.displayRotation + DISPLAY_ROTATION_SPEED);
    }
}