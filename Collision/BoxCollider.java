package Collision;

import GameObjects.Sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class BoxCollider {
    public float centerX, centerY;
    public int width, height;

    public BoxCollider(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;

        centerX = x + (float)(width / 2);
        centerY = y + (float)(height / 2);
    }

    public void drawBox(Graphics g) {
        g.setColor(Color.red);
        g.drawRect((int)centerX - (width / 2), (int)centerY - (height / 2), width, height);
    }

    public float getLeft() {
        return centerX - (float)(width / 2);
    }

    public void setLeft(float newLeft) {
        centerX = newLeft + (float)(width / 2);
    }

    public float getRight() {
        return centerX + (float)(width / 2);
    }

    public void setRight(float newRight) {
        centerX = newRight - (float)(width / 2);
    }

    public float getTop() {
        return centerY - (float)(height / 2);
    }

    public void setTop(float newTop) {
        centerY = newTop + (float)(height / 2);
    }

    public float getBottom() {
        return centerY + (float)(height / 2);
    }

    public void setBottom(float newBottom) {
        centerY = newBottom - (float)(height / 2);
    }

    public static boolean checkCollision(Sprite s1, Sprite s2) {
        boolean noXOverlap = s1.box.getRight() <= s2.box.getLeft() || s1.box.getLeft() >= s2.box.getRight();
        boolean noYOverlap = s1.box.getBottom() <= s2.box.getTop() || s1.box.getTop() >= s2.box.getBottom();


        return !(noXOverlap || noYOverlap);
    }

    public static ArrayList<Sprite> checkCollisionList(Sprite sprite, ArrayList<Sprite> list) {
        ArrayList<Sprite> collisionList = new ArrayList<>();
        for(Sprite toCollide : list) {
            if(checkCollision(sprite, toCollide)) {
                collisionList.add(toCollide);
            }
        }

        return collisionList;
    }
    
    public static boolean isOnPlatforms(Sprite s, ArrayList<Sprite> walls) {
        s.box.centerY += 5;
        ArrayList<Sprite> colList = checkCollisionList(s, walls);
        s.box.centerY -= 5;

        return !colList.isEmpty();
    }

    /*
     * TODO
     * 
     * Currently, the method checks every platform the player is colliding with, and adds it to an arraylist called colList to check
     * however, it's only getting the first sprite from that colList and setting the bottom of the sprite to the top of the colList sprite
     * because, the if statement checks the velocity of the player to tell which direction the sprite is moving and changes the position accordingly
     * The bug is that everything the sprite is colliding with is added to the first colList, and then since we're only checking the first sprite in the list
     * that first sprite is causing the player to be moved on top of the y-level of the wall and this happens because the gravity being added to the player's
     * y-velocity satisfies the if statement
     */
    public static void resolvePlatformCollisions(Sprite s, ArrayList<Sprite> walls) {
        s.yVel += s.GRAVITY;
        
        //Vertical collision checks
        s.box.centerY += s.yVel;
        ArrayList<Sprite> colList = BoxCollider.checkCollisionList(s, walls);
        if(!colList.isEmpty()) {
            Sprite collided = colList.getFirst();
            if(s.yVel > 0) {
                s.box.setBottom(collided.box.getTop());
            } else if(s.yVel < 0) {
                s.box.setTop(collided.box.getBottom());
            }
            s.yVel = 0;
        }

        
        //Horizontal collision check
        s.box.centerX += s.xVel;
        colList = BoxCollider.checkCollisionList(s, walls);
        if(!colList.isEmpty()) {
            Sprite collided = colList.getFirst();
            if(s.xVel > 0) {
                s.box.setRight(collided.box.getLeft());
            } else if(s.xVel < 0){
                s.box.setLeft(collided.box.getRight());
            }
        }
    }
}