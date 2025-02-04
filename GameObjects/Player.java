package GameObjects;

import Enums.Directions;

import java.awt.*;
import java.io.IOException;

public class Player extends Sprite{
    public int hitpoints;
    private Directions direction;

    public Player(String imageFile, Point origin) throws IOException {
        super(imageFile, origin);

        hitpoints = 3;
        direction = Directions.RIGHT;
    }

    public void selectDirection() {
        if(xVel > 0)
            direction = Directions.RIGHT;
        else if(xVel < 0)
            direction = Directions.LEFT;
    }
}