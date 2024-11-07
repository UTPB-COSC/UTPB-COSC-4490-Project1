package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameCanvas extends JPanel implements Runnable {
    private Game game;

    public GameCanvas(Game game) {
        this.game = game;

        // Handle window resizing
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                repaint(); 
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //map draw
        for (int y = 0; y < game.mapHeight; y++) {
            for (int x = 0; x < game.mapWidth; x++) {
                game.map[y][x].draw(g, x * game.tileSize, y * game.tileSize, game.tileSize);
            }
        }

        // player draw
        game.player.draw(g);

        // enemies
        for (Enemy enemy : game.enemies) {
            if (enemy != null) {
                enemy.draw(g);
            }
        }

        // timer top center
        if (game.timerRunning) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String timeLeft = String.format("%02d:%02d",
                (game.timer - (System.currentTimeMillis() - game.startTime) / 1000) / 60,
                (game.timer - (System.currentTimeMillis() - game.startTime) / 1000) % 60);
            g.drawString("Time Left: " + timeLeft,
                (getWidth() - g.getFontMetrics().stringWidth("Time Left: " + timeLeft)) / 2, 30);
        }

        if (game.gameState == GameState.PAUSED || game.gameOver) {
            game.drawMenu(g);
        }
    }

    @Override
    public void run() {
        
        while (game.running) {
            repaint();
            try {
                Thread.sleep(1000 / (int) game.rate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
