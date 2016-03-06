package org.gearvrf.animation.keyframe;

public abstract class GVRAnimationController {
    private static final float TOL = 1e-6f;
    protected GVRKeyFrameAnimation animation;

    public GVRAnimationController(GVRKeyFrameAnimation animation) {
        this.animation = animation;
    }

    /**
     * Update animation at time {@code timeInSeconds}. This function handles
     */
    public void animate(float timeInSeconds) {
        float ticksPerSecond;
        float timeInTicks;

        if (animation.mTicksPerSecond != 0) {
            ticksPerSecond = (float) animation.mTicksPerSecond;
        } else {
            ticksPerSecond = 25.0f;
        }
        timeInTicks = timeInSeconds * ticksPerSecond;

        float animationTime = timeInTicks % (animation.mDurationTicks + TOL); // auto-repeat
        animateImpl(animationTime);
    }

    /**
     * Animate to a time in the time frame.
     * @param animationTime Time in seconds. This falls within the animation's
     * time frame.
     */
    protected abstract void animateImpl(float animationTime);
}
