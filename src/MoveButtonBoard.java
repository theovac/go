import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MoveButtonBoard {
    class Intersection extends JButton implements MouseListener {
        private JButton moveSource = null;
        private boolean occupied = false;
        private int playerId = -1;

        public Intersection() {
            setPreferredSize(new Dimension(60, 60));
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            addMouseListener(this);
            setVisible(true);
        }

        public JButton getMoveSource() {
            return moveSource;
        }

        public void occupy(int playerId, ImageIcon playerIcon) {
            this.setIcon(playerIcon);
            this.occupied = true;
            this.playerId = playerId;
        }

        public boolean isOccupied() { return this.occupied; }

        public int getPlayerId() { return this.playerId; }

        public void init() {
            this.playerId = -1;
            this.occupied = false;
            this.moveSource = null;
        }

        public void mouseClicked(MouseEvent e) {
            this.moveSource = (JButton)e.getSource();
            this.occupied = true;
            this.setIcon(null);
        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
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
        this.moveButtons[i][i].occupy(playerId, playerIcon);
    }

    public void initButton(int i, int j) {
        this.moveButtons[i][j].init();
    }

    public GoRules.BoardPosition getMovePosition(int playerId, ImageIcon playerIcon) {
        while (true) {
            for (int i=0; i<this.moveButtons.length; i++) {
                for (int j=0; j<this.moveButtons.length; j++) {
                    if (moveButtons[i][j].getMoveSource() != null && !moveButtons[i][j].isOccupied()) {
                        moveButtons[i][j].occupy(playerId, playerIcon);
                        return new GoRules.BoardPosition(i, j);
                    }
                }
            }
        }
    }
}

