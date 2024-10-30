package src;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class Tank
{
    Game game;
    Toolkit tk;

    public int yPos;
    public int xPos;

    public double yVel = 0.0;
    public double yAcc = 0.1;
    public double xVel = 0.0;
    public double xAcc = 0.1;
    public double maxVel = 5.0;

    public double targetRotation = 0; // Target angle based on velocity
    public double currentRotation = 0; // Current rotation angle
    public double rotationSpeed = 0.1; // Per Frame rotation rate

    public boolean movingNorth = false;
    public boolean movingSouth = false;
    public boolean movingWest = false;
    public boolean movingEast = false;

    public int width;
    public int height;
    private int xGrace = 15;
    private int yGrace = 15;

    private BufferedImage[] tankBodyImage = new BufferedImage[1];
    private BufferedImage[] tankTurretImage = new BufferedImage[1];
    /*                      Animation Framework left in place for future use.
    private int animframe = 0;
    private int animRate = 4;
    private int frameCount = 0;
    */

    public Tank(Game g, Toolkit tk) throws IOException {
        game = g;
        this.tk = tk;

        tankBodyImage[0] = ImageIO.read(new File("TankBody.png"));

        width = tankBodyImage[0].getWidth();
        height = tankBodyImage[0].getHeight(); 

        //xGrace = Math.max(xGrace, width / 14);
        //yGrace = Math.max(yGrace, height / 20);

        /*                                                  Animation Framework left in place for future use.
        int fragHeight = image.getHeight() / 4;

        for (int i = 0; i < 4; i++)
        {
            Image temp = image.getSubimage(0, i * fragHeight, image.getWidth(), fragHeight).getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            birdImage[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics x = birdImage[i].getGraphics();
            x.drawImage(temp, 0, 0, null);
            x.dispose();
        }*/

        tankTurretImage[0] = ImageIO.read(new File("TankTurret.png"));

        reset();
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public void drawTank(Graphics g)
    {
        // Draw tank body
        // Calculate the angle in radians based on the velocity vector
        if (xVel != 0 || yVel != 0) {
            targetRotation = Math.atan2(yVel, xVel);
        }

        // Calculate the shortest rotation direction
        double angleDifference = normalizeAngle(targetRotation - currentRotation);
        // Normalize both angles to avoid unnecessary long rotation
        currentRotation = normalizeAngle(currentRotation);
        targetRotation = normalizeAngle(targetRotation);
            
        // Smoothly rotate towards the target rotation using the shortest path; + clockwise, - counterclockwise
        if (Math.abs(angleDifference) > rotationSpeed) {
            if (angleDifference > 0) {
                currentRotation += rotationSpeed;
            } else {
                currentRotation -= rotationSpeed;
            }
        } else {
            // If the angle difference is small enough, snap to the target rotation
            currentRotation = targetRotation;
        }
            
        // Apply rotation to image using AffineTransform
        AffineTransform at = new AffineTransform();
        at.rotate(currentRotation, width / 2, height / 2);
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        /*                              Animation Framework left in place for future use.
        frameCount++;
        animRate = Math.max((int)(4 + yVel), 1);
        if (frameCount % animRate == 0)
            animframe++;
        if(animframe >= birdImage.length)
            animframe = 0;
        */

        BufferedImage tankBody = ato.filter(tankBodyImage[0], null);
        g.drawImage(tankBody, xPos, yPos, null);

        // Draw tank turret
        // Calculate the angle between the tank's position and the mouse cursor
        double mouseAngle = Math.atan2(game.mouseY - (yPos + height / 2), game.mouseX - (xPos + width / 2));

        // Apply rotation using AffineTransform
        AffineTransform atMouse = new AffineTransform();
        atMouse.rotate(mouseAngle, width / 2, height / 2);
        AffineTransformOp atoMouse = new AffineTransformOp(atMouse, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage tankTurret = atoMouse.filter(tankTurretImage[0], null);
        g.drawImage(tankTurret, xPos, yPos, null);

        if (game.debug)
        {
            g.setColor(Color.RED);
            g.drawRect(xPos + xGrace, yPos + yGrace, width - xGrace * 2, height - yGrace * 2);
        }
    }

    public void fire()
    {
        //implement firing of gun
    }

    public boolean collide(Bomber bomber)
    {
        if (yPos < 0)
        {
            movingNorth = false;
            yVel = 0;
        }
        if (yPos + height > tk.getScreenSize().height)
        {
            movingSouth = false;
            yVel = 0;
        }
        if (xPos < 0)
        {
            movingWest = false;
            xVel = 0;
        }
        if (xPos + height > tk.getScreenSize().width)
        {
            movingEast = false;
            xVel = 0;
        }
        // Check if the player's bounding box overlaps horizontally with the bomber's bounding box
        if (xPos + width - xGrace < bomber.xPos || xPos + xGrace > bomber.xPos + bomber.width) {
            return false;
        }
        
        // Check for vertical overlap with the top & bottom bomber
        boolean overlapsTopBomber = yPos + height - yGrace > bomber.yPos
            && yPos + yGrace < bomber.yPos + bomber.width;
        boolean overlapsBottomBomber = yPos + height - yGrace > (bomber.yPos + bomber.width + bomber.gap)
            && yPos + yGrace < (bomber.yPos + 2 * bomber.width + bomber.gap);

        if (overlapsTopBomber || overlapsBottomBomber) {
            collide();
            return true;
        }
        return false;
    }

    private void collide()
    {
        new Thread(() ->
        {
            try
            {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("collide.wav").getAbsoluteFile());
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(20f * (float) Math.log10(game.volume));
                clip.start();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }).start();
    }

    // Movement flags per direction
    public void moveNorth() {
        movingNorth = true;
    }
    
    public void moveSouth() {
        movingSouth = true;
    }
    
    public void moveWest() {
        movingWest = true;
    }
    
    public void moveEast() {
        movingEast = true;
    }
    
    public void stopNorth() {
        movingNorth = false;
    }
    
    public void stopSouth() {
        movingSouth = false;
    }
    
    public void stopWest() {
        movingWest = false;
    }
    
    public void stopEast() {
        movingEast = false;
    }

    public void reset()
    {
        xPos = tk.getScreenSize().width / 2;
        yPos = tk.getScreenSize().height / 2;

        movingEast = false;
        movingWest = false;
        movingNorth = false;
        movingSouth = false;

        yVel = 0.0;
        xVel = 0.0;
        targetRotation = 0.0;
        currentRotation = 0.0;
    }

    public void update()
    {
    // Vertical movement
    if (movingNorth) {
        yVel -= yAcc; // Accelerate upwards
        if (yVel < -maxVel) yVel = -maxVel; // Cap at max upwards velocity
    } else if (movingSouth) {
        yVel += yAcc; // Accelerate downwards
        if (yVel > maxVel) yVel = maxVel; // Cap at max downwards velocity
    } else {
        // Decelerate if no vertical movement
        if (yVel > 0) {
            yVel -= yAcc; // Decelerate downwards
            if (yVel < 0) yVel = 0; // Stop at 0
        } else if (yVel < 0) {
            yVel += yAcc; // Decelerate upwards
            if (yVel > 0) yVel = 0; // Stop at 0
        }
    }

    // Horizontal movement
    if (movingWest) {
        xVel -= xAcc; // Accelerate left
        if (xVel < -maxVel) xVel = -maxVel; // Cap at max leftwards velocity
    } else if (movingEast) {
        xVel += xAcc; // Accelerate right
        if (xVel > maxVel) xVel = maxVel; // Cap at max rightwards velocity
    } else {
        // Decelerate if no horizontal movement
        if (xVel > 0) {
            xVel -= xAcc; // Decelerate rightwards
            if (xVel < 0) xVel = 0; // Stop at 0
        } else if (xVel < 0) {
            xVel += xAcc; // Decelerate leftwards
            if (xVel > 0) xVel = 0; // Stop at 0
        }
    }

    // Update position based on velocities
    xPos += xVel;
    yPos += yVel;
    }
}
