package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Enemy {
    public int x, y;  // Enemy's position
    private Game game;
    private BufferedImage enemyImg;  
    private Random random = new Random();  //moving randomly
    private long lastMoveTime = 0;  // time since move
    private final long moveCooldown = 100;  // .1 sec move for enemy

    public Enemy(int x, int y, Game game) {
        this.x = x;
        this.y = y;
        this.game = game;

        try {
            enemyImg = ImageIO.read(new File("enemy_game.png"));
        } catch (IOException e) {
            e.printStackTrace();
            enemyImg = null;
        }
        lastMoveTime = System.currentTimeMillis();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastMoveTime >= moveCooldown) {
            int direction = random.nextInt(4);  //random number 0 to 3

            switch (direction) {
                case 0:  // up
                    if (canMoveTo(x, y - 1)) y--;
                    break;
                case 1:  //down
                    if (canMoveTo(x, y + 1)) y++;
                    break;
                case 2:  // left
                    if (canMoveTo(x - 1, y)) x--;
                    break;
                case 3:  //right
                    if (canMoveTo(x + 1, y)) x++;
                    break;
            }

            lastMoveTime = currentTime;  // enemy timer reset
        }
    }

    
    private boolean canMoveTo(int newX, int newY) {
        if (newX >= 0 && newX < game.mapWidth && newY >= 0 && newY < game.mapHeight) {
            return game.map[newY][newX].type == Tile.Type.EMPTY;
        }
        return false;
    }


    public void draw(Graphics g) {
        if (enemyImg != null) {
            g.drawImage(enemyImg, x * game.tileSize, y * game.tileSize, game.tileSize, game.tileSize, null);
        } else {
            //red Tile if no image loads
            g.setColor(Color.RED);
            g.fillRect(x * game.tileSize, y * game.tileSize, game.tileSize, game.tileSize);
        }
    }
}
