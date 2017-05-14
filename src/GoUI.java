import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GoUI {

    BufferedImage blackImg, whiteImg;
    private static ImageIcon blackIcon, whiteIcon;

    private static int boardSize;
    private static int[][] gameState;
    private static Intersection[][] buttonArray;
    private static int currentPlayerId;
    private static ImageIcon currentPlayerIcon;
    private static volatile boolean waitingForTurn = true;
    private static JButton moveButton;
    private static Index moveIndex = new Index(-1, -1);

    public GoUI(int boardSize){
        this.boardSize = boardSize;
        this.gameState = new int[this.boardSize][this.boardSize];
        this.buttonArray = new Intersection[this.boardSize][this.boardSize];

        try {
            initUI();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Board image not found");
        }
    }

    private final void initUI() throws IOException{
        loadImages();
        JFrame mainFrame = new JFrame("GO");
        mainFrame.getContentPane().setLayout(new BorderLayout());
        mainFrame.setContentPane(new JPanel() { BufferedImage image =
                ImageIO.read(new URL("http://go.alamino.net/aprendajogargo/images/Blank_Go_board_9x9.png"));
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image.getScaledInstance(
                    600, 600, image.SCALE_DEFAULT), 0, 0, 600, 600, this);
            }

        });

        interBhv(mainFrame);
        JMenuBar menuBar = createMenuBar();
        mainFrame.setJMenuBar(menuBar);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(615, 640);
        mainFrame.pack();
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
    }

    private void loadImages() throws IOException {
        blackImg = ImageIO.read(new File("black.png"));
        whiteImg = ImageIO.read(new File("white.png"));
        blackIcon = new ImageIcon(blackImg.getScaledInstance(60, 60, blackImg.SCALE_DEFAULT));
        whiteIcon = new ImageIcon(whiteImg.getScaledInstance(60, 60, whiteImg.SCALE_DEFAULT));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu game = new JMenu("Game");
        JMenuItem restart = new JMenuItem("Restart");
        restart.setMnemonic(KeyEvent.VK_R);
        restart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGameState();
            }
        });

        JMenuItem quit = new JMenuItem("Quit");
        quit.setMnemonic(KeyEvent.VK_Q);
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        game.add(restart);
        game.add(quit);
        menuBar.add(game);

        return menuBar;

    }

    private void interBhv(JFrame mainFrame) throws IOException {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(9, 9, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.setOpaque(false);
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                buttonArray[i][j] = new Intersection();
                buttonPanel.add(buttonArray[i][j]);
            }
        }

        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void drawState() {
       for (int i = 0; i < boardSize; i++) {
           for (int j = 0; j < boardSize; j++) {
               if (this.gameState[i][j] == 1) {
                   buttonArray[i][j].setIcon(blackIcon);
                   buttonArray[i][j].hoverStatus = false;
               }
               else if (this.gameState[i][j] == 2) {
                   buttonArray[i][j].setIcon(whiteIcon);
                   buttonArray[i][j].hoverStatus = false;
               }
               else {
                   buttonArray[i][j].setIcon(null);
                   buttonArray[i][j].hoverStatus = true;
               }
           }
       }
    }

    private static void updateGameState() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (buttonArray[i][j].getIcon() == whiteIcon) {
                    gameState[i][j] = 2;
                }
                else if (buttonArray[i][j].getIcon() == blackIcon) {
                    gameState[i][j] = 1;
                }
            }
        }
    }

    // UI-Controller communication methods.
   public int[][] getGameState() {
       return this.gameState;
  }

   public void setGameState(int[][] gameState) {
       for (int i = 0; i < boardSize; i++) {
           for (int j = 0; j < boardSize; j++) {
               this.gameState[i][j] = gameState[i][j];
           }
       }
       drawState();
   }

    public void resetGameState() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                this.gameState[i][j] = 0;
            }
        }
        drawState();
    }

    public Index getTurn(int id) {
        waitingForTurn = true;
        currentPlayerIcon = (id == 1) ? blackIcon : whiteIcon;
        while(true) {
            if (!waitingForTurn) {
                for(int i = 0; i < boardSize; i++) {
                    for (int j = 0; j < boardSize; j++) {
                        if (buttonArray[i][j] == moveButton) {
                            moveIndex.x = i;
                            moveIndex.y = j;
                            //System.out.println("Got moveIndex");
                        }
                    }
                }

               return moveIndex;
            }
        }
    }

    public boolean checkSelfCapture(Index index) {
        boolean result = false;
        int i  = index.x;
        int j = index.y;
        if (i > 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 1 && gameState[i - 1][j] == 2
                && gameState[i + 1][j] == 2 && gameState[i][j - 1] == 2
                && gameState[i][j + 1] == 2) {
            result = true;
        } else if (i > 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 2 && gameState[i - 1][j] == 1
                && gameState[i + 1][j] == 1 && gameState[i][j - 1] == 1
                && gameState[i][j + 1] == 1) {
            result = true;
        } else if (i == 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 1
                && gameState[i + 1][j] == 2 && gameState[i][j - 1] == 2
                && gameState[i][j + 1] == 2) {
            result = true;
        } else if (i == 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 2
                && gameState[i + 1][j] == 1 && gameState[i][j - 1] == 1
                && gameState[i][j + 1] == 1) {
            result = true;
        } else if (i > 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 1 && gameState[i - 1][j] == 2
                && gameState[i + 1][j] == 2
                && gameState[i][j + 1] == 2) {
            result = true;
        } else if (i > 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 2 && gameState[i - 1][j] == 1
                && gameState[i + 1][j] == 1
                && gameState[i][j + 1] == 1) {
            result = true;
        } else if (i > 0 && j > 0 && i == boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 1 && gameState[i - 1][j] == 2
                && gameState[i][j - 1] == 2
                && gameState[i][j + 1] == 2) {
            result = true;
        } else if (i > 0 && j > 0 && i == boardSize - 1 && j < boardSize - 1
                && gameState[i][j] == 2 && gameState[i - 1][j] == 1
                && gameState[i][j - 1] == 1
                && gameState[i][j + 1] == 1) {
            result = true;
        } else if (i > 0 && j > 0 && i < boardSize - 1 && j == boardSize - 1
                && gameState[i][j] == 1 && gameState[i - 1][j] == 2
                && gameState[i + 1][j] == 2 && gameState[i][j - 1] == 2) {
            result = true;
        } else if (i > 0 && j > 0 && i < boardSize - 1 && j == boardSize - 1
                && gameState[i][j] == 2 && gameState[i - 1][j] == 1
                && gameState[i + 1][j] == 1 && gameState[i][j - 1] == 1) {
            result = true;
        }
        else if (i > 0 && j == 0 && i == boardSize - 1 && j < boardSize - 1
                && gameState[i][j] ==  1 && gameState[i-1][j] == 2
                && gameState[i][j + 1] == 2) {
            result = true;
        }
        else if (i > 0 && j == 0 && i == boardSize - 1 && j < boardSize - 1
                && gameState[i][j] ==  2 && gameState[i-1][j] == 1
                && gameState[i][j + 1] == 1) {
            result = true;
        }
        else if (i == 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] ==  1
                && gameState[i + 1][j] == 2
                && gameState[i][j + 1] == 2) {
            result = true;
        }
        else if (i == 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                && gameState[i][j] ==  2
                && gameState[i + 1][j] == 1
                && gameState[i][j + 1] == 1) {
            result = true;
        }
        return result;
    }


    public class Intersection extends JButton implements MouseListener {

        boolean hoverStatus = true;

        public Intersection() throws IOException {
            setPreferredSize(new Dimension(60, 60));
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            addMouseListener(this);
            setVisible(true);
        }

        public void mouseClicked(MouseEvent e) {
            this.setIcon(currentPlayerIcon);
            updateGameState();
            hoverStatus = false;
            moveButton = (JButton)e.getSource();
            waitingForTurn = false;

        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {
            if (hoverStatus == true) {
                this.setIcon(currentPlayerIcon);
            }
        }

        public void mouseExited(MouseEvent e) {
            if (hoverStatus == true) {
                this.setIcon(null);
            }
        }
    }

}
