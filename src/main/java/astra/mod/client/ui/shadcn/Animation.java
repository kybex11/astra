package astra.mod.client.ui.shadcn;

public class Animation {
    private float current;
    private float target;
    private float speed;
    private long lastTime;

    public Animation(float initial, float speed) {
        this.current = initial;
        this.target = initial;
        this.speed = speed;
        this.lastTime = System.currentTimeMillis();
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void set(float value) {
        this.current = value;
        this.target = value;
    }

    public float get() {
        long now = System.currentTimeMillis();
        float delta = (now - lastTime) / 1000.0f;
        lastTime = now;

        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            current = target;
        } else {
            current += diff * Math.min(1.0f, delta * speed * 60.0f);
        }
        return current;
    }

    public float getCurrent() {
        return current;
    }

    public float getTarget() {
        return target;
    }

    public boolean isComplete() {
        return Math.abs(target - current) < 0.001f;
    }
}
