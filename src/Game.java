package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game implements Runnable {
    private final GameCanvas canvas;
    public Player player;
    public Enemy[] enemies;
    public Tile[][] map;
    public int mapWidth = 15;
    public int mapHeight = 11; 
    public int tileSize = 50;  
    public boolean running = true;
    public boolean gameOver = false;  
    public double rate = 60.0; 
    public GameState gameState = GameState.RUNNING;
    public long startTime;  // game timer
    public int timer = 60;   // 1 min timer
    public boolean timerRunning = true; 

    public Game() {
        JFrame frame = new JFrame("BombMakeGoBoom");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(mapWidth * tileSize + 200, mapHeight * tileSize + 80);
        frame.setResizable(true);
        frame.setVisible(true);
        timer = 60; 
        startTime = System.currentTimeMillis();
        timerRunning = true; 

        //character positions
        map = new Tile[mapHeight][mapWidth];    
        player = new Player(1, 1, this);  
        enemies = new Enemy[5];           
       
        

        initMap();
        resetGame();
        canvas = new GameCanvas(this);
        frame.add(canvas);
        canvas.setPreferredSize(new Dimension(mapWidth * tileSize, mapHeight * tileSize));

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        Thread drawLoop = new Thread(canvas);
        drawLoop.start();
    }

    // Buttons for game menu
    private void handleKeyPress(KeyEvent e) {
        if (gameState == GameState.RUNNING) {
            player.keyPressed(e);  
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {  
                gameState = GameState.PAUSED;
            }
        } else if (gameState == GameState.PAUSED) {
            if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE) {  
                gameState = GameState.RUNNING;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_N) {  
            resetGame();
        }
        if (e.getKeyCode() == KeyEvent.VK_E) { 
            System.exit(0);
        }
    
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_N) {  
                resetGame();
            }
            if (e.getKeyCode() == KeyEvent.VK_E) { 
                System.exit(0);
            }
        }
    }
    

    // maps and blocks
    private void initMap() {
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                if (y == 0 || y == mapHeight - 1 || x == 0 || x == mapWidth - 1 || (x % 2 == 0 && y % 2 == 0)) {
                    map[y][x] = new Tile(Tile.Type.WALL);  
                } else {
                    if (Math.random() < 0.2) {
                        map[y][x] = new Tile(Tile.Type.BLOCK);  // Breakable block
                    } else {
                        map[y][x] = new Tile(Tile.Type.EMPTY);  // Empty space
                    }
                }
            }
        }
        // clearing Blocks around the player
        map[1][1] = new Tile(Tile.Type.EMPTY);
        map[1][2] = new Tile(Tile.Type.EMPTY);
        map[2][1] = new Tile(Tile.Type.EMPTY);
        // clear around enemy
        map[9][12] = new Tile(Tile.Type.EMPTY);
        map[9][13] = new Tile(Tile.Type.EMPTY);
        map[8][13] = new Tile(Tile.Type.EMPTY);
    }


    public void resetGame() {
        running = true;  
        gameOver = false;  
        gameState = GameState.RUNNING;  
        
        //timer restting
        startTime = System.currentTimeMillis(); 
        timer = 60; 
       
        
        
        player.x = 1;
        player.y = 1;

       
        initMap();
        enemies[0] = new Enemy(13, 9, this);
        // enemies[1] = new Enemy(9, 9, this);
        // enemies[2] = new Enemy(1, 5, this);
        // enemies[3] = new Enemy(7, 9, this);
        // enemies[4] = new Enemy(5, 7, this);
        
    }

    public void checkCollision() {
        for (Enemy enemy : enemies) {
            if (enemy != null && player.x == enemy.x && player.y == enemy.y) {
                gameOver(); 
            }
        }
    }

    // gameover
    public void gameOver() {
        running = false;  
        gameOver = true;  
        gameState = GameState.GAME_OVER;
    }

    // drawing blocks
    public void drawMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));  // Semi-transparent black
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());  // Overlay

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        if (gameState == GameState.PAUSED) {
            g.drawString("PAUSED", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 - 50);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("Continue Playing(P)", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 + 10);
            g.drawString("New Game(N)", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 + 50);
            g.drawString("Exit(E)", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 + 90);
        } else if (gameState == GameState.GAME_OVER) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", (canvas.getWidth() / 2) - 120, canvas.getHeight() / 2 - 50);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("New Game(N)", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 + 10);
            g.drawString("Exit(E)", (canvas.getWidth() / 2) - 100, canvas.getHeight() / 2 + 50);
        }
    }
    @Override
    public void run() {
        while (true) {
            if (gameState == GameState.RUNNING && running) {
                player.update();
                for (Enemy enemy : enemies) {
                    if (enemy != null) {
                        enemy.update();
                    }
                }
                checkCollision();

                // Update the timer
                if (timerRunning) {
                    long remTime = (System.currentTimeMillis() - startTime) / 1000;
                    if (remTime >= timer) {
                        gameOver(); // End the game when the timer runs out
                    }
                }

                canvas.repaint();
            }

            try {
                Thread.sleep((long) (1000.0 / rate));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        Game game = new Game();
        Thread logicLoop = new Thread(game);
        logicLoop.start();
    }
}
