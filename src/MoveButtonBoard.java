import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;

public class MoveButtonBoard {
    class Intersection extends JButton implements MouseListener {
        private JButton moveSource = null;
        private boolean occupied = false;
        private int playerId = -1;
        private BufferedImage blackStoneImg;
        private ImageIcon blackStoneIcon;

        public Intersection() {
            setPreferredSize(new Dimension(60, 60));
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            addMouseListener(this);
            setVisible(true);
            try {
                this.blackStoneImg = ImageIO.read(new File("black.png"));
                this.blackStoneIcon = new ImageIcon(blackStoneImg.getScaledInstance(60, 60, blackStoneImg.SCALE_DEFAULT));
            } catch (Exception e) {

            }
        }

        public JButton getMoveSource() {
            return moveSource;
        }

        public void occupy(int playerId, ImageIcon playerIcon) {
            if (!this.isOccupied()) {
                this.setIcon(playerIcon);
                this.occupied = true;
                this.playerId = playerId;
            }
        }

        public boolean isOccupied() { return this.occupied; }

        public int getPlayerId() { return this.playerId; }

        public void init() {
            this.playerId = -1;
            this.occupied = false;
            this.moveSource = null;
            this.setIcon(null);
        }

        public void mouseClicked(MouseEvent e) {
            if (this.isOccupied()) { return; }
            this.moveSource = (JButton)e.getSource();
        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {
            if (!this.isOccupied()) {
                this.setIcon(this.blackStoneIcon);
            }
        }

        public void mouseExited(MouseEvent e) {
            if (!this.isOccupied()) {
                this.setIcon(null);
            }
        }
    }
    private Intersection[][] moveButtons;

    public MoveButtonBoard(int size) {
        this.moveButtons = new Intersection[size][size];
        for (int i=0; i<this.moveButtons.length; i++) {
            for (int j = 0; j < this.moveButtons.length; j++) {
                this.moveButtons[i][j] = new Intersection();
            }
        }
    }

    public void init() {
        for (int i=0; i<this.moveButtons.length; i++) {
            for (int j = 0; j < this.moveButtons.length; j++) {
                this.moveButtons[i][j].init();
            }
        }
    }

    public Intersection get(int i, int j) { return this.moveButtons[i][j]; }

    public void occupyButton(int i, int j, int playerId, ImageIcon playerIcon) {
        this.moveButtons[i][j].occupy(playerId, playerIcon);
    }

    public void initButton(int i, int j) {
        this.moveButtons[i][j].init();
    }

    public GoRules.BoardPosition getMovePosition(int playerId, ImageIcon playerIcon) {
        for (int i=0; i<this.moveButtons.length; i++) {
            for (int j=0; j<this.moveButtons.length; j++) {
                if (moveButtons[i][j].getMoveSource() != null) {
                    return new GoRules.BoardPosition(i, j);
                }
            }
        }
        return null;
    }
}

