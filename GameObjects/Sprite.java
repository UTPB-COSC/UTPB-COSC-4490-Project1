package GameObjects;

import Collision.BoxCollider;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


public class Sprite {
    public final float GRAVITY = 0.6f;

    BufferedImage playerImage;
    public BoxCollider box;
    public float xVel, yVel;

    public Sprite(String imageFile, Point origin, int w, int h) throws IOException {
        box = new BoxCollider(origin.x, origin.y, w, h);

        playerImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics x = playerImage.getGraphics();
        BufferedImage rawImage = ImageIO.read(new File(imageFile));
        Image tempImage = rawImage.getScaledInstance(box.width, box.height, Image.SCALE_SMOOTH);
        x.drawImage(tempImage, 0, 0, null);
        x.dispose();
        xVel = 0.0f;
        yVel = 0.0f;
    }

    public Sprite(String imageFile, Point origin) throws IOException{
        this(imageFile, origin, 100, 100);
    }

    public void draw(Graphics g) {
        g.drawImage(playerImage, (int)box.centerX - (box.width / 2), (int)box.centerY - (box.height / 2), null);
    }

    public void moveHorizontal() {
        box.centerX += xVel;
    }

    //Debug method
    public void dMoveLeft() {
        xVel -= 0.1f;
        moveHorizontal();
    }

    public void reset() {
        xVel = 0;
        yVel = 0;
    }

    public void update() {
        box.centerX += xVel;
        box.centerY += yVel;
    }
}
