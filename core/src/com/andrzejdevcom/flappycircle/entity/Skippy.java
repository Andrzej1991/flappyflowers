package com.andrzejdevcom.flappycircle.entity;

import com.andrzejdevcom.flappycircle.config.GameConfig;
import com.badlogic.gdx.math.Circle;

public class Skippy {

    private final Circle collistionCircle;
    private float x;
    private float y;
    private float ySpeed;

    public Skippy() {
        collistionCircle = new Circle(x, y, GameConfig.SKIPPY_HALF_SIZE);
    }

    public Circle getCollistionCircle() {
        return collistionCircle;
    }


    public void update(float dt) {
        ySpeed -= GameConfig.DIVE_ACC * dt;
        setY(y + ySpeed);
    }

    public float getX() {
        return x;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateCollisionCircle();
    }

    public void flyUp() {
        ySpeed = GameConfig.FLY_ACC;
        setY(y + ySpeed);
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        updateCollisionCircle();
    }

    private void updateCollisionCircle() {
        collistionCircle.setX(x);
        collistionCircle.setY(y);
    }
}
