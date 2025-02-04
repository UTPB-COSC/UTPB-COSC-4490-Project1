package src;

import GameObjects.*;
import LevelEditor.*;
import Collision.BoxCollider;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

/*
 * Class responsible for handling all the game logic, entry point for program
 */
public class Game implements Runnable
{
    public Toolkit tk;
    private final GameCanvas canvas;
    public final int SPRITE_SIZE = 100;

    //Player movement clamps and flags
    public Player player;
    private boolean goRight = false;
    private boolean goLeft = false;
    public final double MAX_X_SPEED = 5;
    public final double ACCELERATION = 0.1;
    public final float JUMP_SPEED = 14f;

    //Related to fps
    private final double rateTarget = 100.0;
    public double waitTime = 1000.0 / rateTarget;
    public double rate = 1000 / waitTime;

    //Array objects needed to load level data
    public ArrayList<Sprite> platforms;
    public ArrayList<Enemy> enemies;

    //Misc.
    public boolean debug = false;
    public boolean running = true;
    public boolean isGameOver = false;
    public double volume = 0.3;

    public Game()
    {
        //Initializes a JFrame
        JFrame frame = new JFrame("Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);

        //Gets Screen Size
        tk = Toolkit.getDefaultToolkit();
        System.out.println("Screen Width: " + tk.getScreenSize().width + ", Screen Height: " + tk.getScreenSize().height);

        frame.setVisible(true);
        frame.requestFocus();

        //From this point on, the program can draw to the JFrame
        canvas = new GameCanvas(this, frame.getGraphics());
        frame.add(canvas);

        //Starts loading file data into necessary arrays
        try {
            platforms = new ArrayList<>();
            enemies = new ArrayList<>();

            //I have to do a funky workaround for pass by reference when loading player data
            Player[] playerRef = new Player[1];
            LevelLoader.readCSV(new File(".\\LevelEditor\\test_level.csv"), SPRITE_SIZE, platforms, enemies, playerRef);

            //LevelLoader.createLevel(tileGrid, SPRITE_SIZE, platforms, enemies, playerRef);
            player = playerRef[0];

            //Debug print statements
            System.out.println("Platform Amount: " + platforms.size());
            System.out.println("Enemies Amount: " + enemies.size());
            System.out.println("Player Center X: " + player.box.centerX + ", Player Center Y: " + player.box.centerY);
            System.out.println("Player Top: " + player.box.getTop() + ", Player Bottom: " + player.box.getBottom() + ", Player Left: " + player.box.getLeft() + ", Player Right: " + player.box.getRight());
        } catch (IOException e) {
            System.err.println("Error reading file");
            System.exit(0);
        }

        //Startins drawing to the JFrame
        Thread drawLoop = new Thread(canvas);
        drawLoop.start();

        //Player controls
        frame.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {

            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_SPACE -> {
                        if(running) {
                            if(BoxCollider.isOnPlatforms(player, platforms)) {
                                player.yVel -= JUMP_SPEED;
                            }
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> {
                        running = !running;
                    }
                    case KeyEvent.VK_UP -> {
                        if(!running) {
                            canvas.cursor--;
                            canvas.cursor = Math.max(canvas.cursor, 0);
                        }
                    }
                    case KeyEvent.VK_DOWN -> {
                        if(!running) {
                            canvas.cursor++;
                            canvas.cursor = Math.min(canvas.cursor, 6);
                        }
                    }
                    case KeyEvent.VK_RIGHT -> {
                        if(!running) {
                            if(canvas.cursor == 2) {
                                volume += 0.1;
                                volume = Math.min(volume, 1.0);
                            }
                        }
                    }
                    case KeyEvent.VK_LEFT -> {
                        if(!running) {
                            if(canvas.cursor == 2) {
                                volume -= 0.1;
                                volume = Math.max(volume, 0.0);
                            }
                        }
                    }
                    case KeyEvent.VK_ENTER -> {
                        if(!running) {
                            switch(canvas.cursor) {
                                case 0 -> { reset(); }
                                case 1 -> { System.exit(0); }
                                case 6 -> { debug = !debug; }
                            }
                        }
                    }
                    case KeyEvent.VK_W -> {

                    }
                    //Move Left
                    case KeyEvent.VK_A -> {
                        goLeft = true;
                    }
                    case KeyEvent.VK_S -> {

                    }
                    //Move Right
                    case KeyEvent.VK_D -> {
                        goRight = true;
                    }
                    //Enables and disables debug
                    case KeyEvent.VK_1 -> {
                        debug = !debug;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                switch(e.getKeyCode()) {
                    //Stops moving left
                    case KeyEvent.VK_A -> {
                        player.xVel = 0;
                        goLeft = false;
                    }
                    //Stops moving right
                    case KeyEvent.VK_D -> {
                        player.xVel = 0;
                        goRight = false;
                    }
                }
            }
        });
    }

    //Game Logic starts
    @Override
    public void run()
    {
        while(true)
        {
            long startTime = System.nanoTime();

            if (running)
            {
                if(goLeft) {
                    player.xVel -= 0.1f;
                    player.moveHorizontal();
                }
                
                if(goRight) {
                    player.xVel += 0.1f;
                    player.moveHorizontal();
                }

                //player.dMoveLeft();

                //Checks for collisions and resolves them
                BoxCollider.resolvePlatformCollisions(player, platforms);
                for(Sprite enemy : enemies) {
                    enemy.update();
                    BoxCollider.resolvePlatformCollisions(enemy, platforms);
                }
            }

            long sleep = (long) waitTime - (System.nanoTime() - startTime) / 1000000;
            rate = 1000.0 / Math.max(waitTime - sleep, waitTime);

            try
            {
                Thread.sleep(Math.max(sleep, 0));
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
                System.exit(0);
            }
        }
    }

    public void reset()
    {
        running = true;
    }

    public static void main(String[] args)
    {
        Game game = new Game();

        Thread logicLoop = new Thread(game);
        logicLoop.start();
    }
}
