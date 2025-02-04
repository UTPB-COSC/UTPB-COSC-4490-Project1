package src;

import GameObjects.*;

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class GameCanvas extends JPanel implements Runnable
{

    private final Game game;
    private final Graphics graphics;

    private final double rateTarget = 60.0;
    private final double waitTime = 1000.0 / rateTarget;
    private double rate = 1000.0 / waitTime;

    public int cursor = 0;

    public Point viewOrigin;
    public static float leftMargin = 60;
    public static float rightMargin = 400;
    public static float verticalMargin = 40;

    int width, height;

    public GameCanvas(Game game, Graphics g)
    {
        this.game = game;
        graphics = g;
        viewOrigin = new Point(0, 0);
    }

    public void scroll() {
        float rightBoundary = viewOrigin.x + width - rightMargin;
        if(game.player.box.getRight() > rightBoundary) {
            viewOrigin.x += game.player.box.getRight() - rightBoundary;
        }

        float leftBoundary = viewOrigin.x + leftMargin;
        if(game.player.box.getLeft() < leftBoundary) {
            viewOrigin.x -= leftBoundary - game.player.box.getLeft();
        }

        float bottomBoundary = viewOrigin.y + height - verticalMargin;
        if(game.player.box.getBottom() > bottomBoundary) {
            viewOrigin.y += game.player.box.getBottom() - bottomBoundary;
        }

        float topBoundary = viewOrigin.y + verticalMargin;
        if(game.player.box.getTop() < topBoundary){
            viewOrigin.y -= topBoundary - game.player.box.getTop();
        }
    }

    @Override
    public void run() {
        while(true) {
            long startTime = System.nanoTime();

            width = game.tk.getScreenSize().width;
            height = game.tk.getScreenSize().height;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setColor(Color.CYAN);
            g2d.fillRect(0, 0, width, height);

            scroll();
            g2d.translate(-viewOrigin.x, -viewOrigin.y);

            game.player.draw(g2d);
            for (Sprite platform : game.platforms) {
                platform.draw(g2d);
            }
            for (Sprite enemy : game.enemies) {
                enemy.draw(g2d);
            }

            g2d.setColor(Color.BLACK);
            if (!game.isGameOver) {
                if (game.running) {
                    //g2d.drawString(String.format("Score: %d", game.score), 25, 25);
                    //g2d.drawString(String.format("High Score: %d", game.highScore), 25, 50);
                } else {
                    g2d.drawString(String.format("%s Reset Game", cursor == 0 ? ">" : " "), viewOrigin.x + 25, viewOrigin.y + 25);
                    g2d.drawString(String.format("%s Exit Game", cursor == 1 ? ">" : " "), viewOrigin.x + 25, viewOrigin.y + 50);
                    String vol = "";
                    for (int i = 0; i < 11; i++) {
                        if ((int) (game.volume * 10) == i) {
                            vol += "|";
                        } else {
                            vol += "-";
                        }
                    }
                    g2d.drawString(String.format("%s Volume %s", cursor == 2 ? ">" : " ", vol), viewOrigin.x + 25, viewOrigin.y + 75);
                    g2d.drawString(String.format("%s Debug Mode %s", cursor == 6 ? ">" : " ", game.debug ? "(ON)" : "(OFF)"), viewOrigin.x + 25, viewOrigin.y + 100);
                }
                if (game.debug) {
                    g2d.drawString(String.format("FPS = %.1f", rate), viewOrigin.x + 200, viewOrigin.y + 25);
                    g2d.drawString(String.format("UPS = %.1f", game.rate), viewOrigin.x + 200, viewOrigin.y + 50);
                    g2d.drawString("Lives: " + game.player.hitpoints, viewOrigin.x + 200, viewOrigin.y + 75);
                    game.player.box.drawBox(g2d);
                    for (Sprite enemy : game.enemies) {
                        enemy.box.drawBox(g2d);
                    }
                    for (Sprite platform : game.platforms) {
                        platform.box.drawBox(g2d);
                    }
                }

                graphics.drawImage(image, 0, 0, null);

                long sleep = (long) waitTime - (System.nanoTime() - startTime) / 1000000;
                rate = 1000.0 / Math.max(waitTime - sleep, waitTime);

                try {
                    Thread.sleep(Math.max(sleep, 0));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    System.exit(0);
                }
            }
        }
    }
}
