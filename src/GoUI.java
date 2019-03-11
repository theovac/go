import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GoUI {
    private BufferedImage blackStoneImg, whiteStoneImg;
    private ImageIcon blackStoneIcon, whiteStoneIcon;
    private JFrame mainFrame;


    private int boardSize;
    public int[][] gameState;
    private MoveButtonBoard moveButtonBoard;
    private GoRules.BoardPosition movePos = new GoRules.BoardPosition(-1, -1);
    private boolean playerPassed = false;
    private JPanel moveButtonPanel;

    public GoUI(int boardSize) {
        this.boardSize = boardSize;
        this.gameState = new int[this.boardSize][this.boardSize];
        this.moveButtonBoard = new MoveButtonBoard(boardSize);

        try {
            this.blackStoneImg = ImageIO.read(new File("black.png"));
            this.whiteStoneImg = ImageIO.read(new File("white.png"));
            this.blackStoneIcon = new ImageIcon(blackStoneImg.getScaledInstance(60, 60, blackStoneImg.SCALE_DEFAULT));
            this.whiteStoneIcon = new ImageIcon(whiteStoneImg.getScaledInstance(60, 60, whiteStoneImg.SCALE_DEFAULT));

            this.mainFrame = new JFrame("GO");
            this.mainFrame.getContentPane().setLayout(new BorderLayout());
            this.mainFrame.setContentPane(new JPanel() {
                BufferedImage boardImg =
                        ImageIO.read(new File("Blank_Go_board_9x9.png"));

                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(boardImg.getScaledInstance(600, 600, Image.SCALE_DEFAULT), 0, 0, null);
                }

            });
            this.moveButtonPanel = new JPanel();
            this.moveButtonPanel.setLayout(new GridLayout(9, 9, 5, 5));
            this.moveButtonPanel.setBorder(BorderFactory.createEmptyBorder());
            this.moveButtonPanel.setOpaque(false);

            this.mainFrame.add(moveButtonPanel, BorderLayout.SOUTH);
            JMenuBar menuBar = createMenuBar();
            this.mainFrame.setSize(615, 640);
            this.mainFrame.setJMenuBar(menuBar);
            this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.mainFrame.pack();
            this.mainFrame.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initUI(){
        // Initialise all clickable move selection buttons.
        this.moveButtonBoard.init();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                moveButtonPanel.add(this.moveButtonBoard.get(i, j));
            }
        }
        this.mainFrame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameTab = new JMenu("Game");
        menuBar.add(gameTab);

        JMenuItem restartButton = new JMenuItem("Restart");
        restartButton.setMnemonic(KeyEvent.VK_R);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGameState();
            }
        });
        gameTab.add(restartButton);

        JMenuItem quitButton = new JMenuItem("Quit");
        quitButton.setMnemonic(KeyEvent.VK_Q);
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        gameTab.add(quitButton);

        JMenuItem passButton = new JMenuItem("Pass");
        passButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                playerPassed = true;
            }
        });
        menuBar.add(passButton);

        return menuBar;

    }

    private void drawState() {
       for (int i = 0; i < boardSize; i++) {
           for (int j = 0; j < boardSize; j++) {
               if (this.gameState[i][j] == 1) {
                   this.moveButtonBoard.occupyButton(i, j, 1, this.blackStoneIcon);
               }
               else if (this.gameState[i][j] == 2) {
                   this.moveButtonBoard.occupyButton(i, j, 2, this.whiteStoneIcon);
               }
               else {
                   this.moveButtonBoard.initButton(i, j);
               }
           }
       }
    }

    private void updateGameState() {
        int buttonPlayerId;
        for (int i = 0; i < this.boardSize; i++) {
            for (int j = 0; j < this.boardSize; j++) {
                buttonPlayerId = this.moveButtonBoard.get(i, j).getPlayerId();
                this.gameState[i][j] = (buttonPlayerId != -1) ? buttonPlayerId : 0;
            }
        }
    }

   public int[][] getGameState() {
       return this.gameState;
  }

   public void setGameState(int[][] gameState) {
       for (int i = 0; i < boardSize; i++) {
           for (int j = 0; j < boardSize; j++) {
               this.gameState[i][j] = gameState[i][j];
           }
       }
       this.drawState();
   }

    public void resetGameState() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                this.gameState[i][j] = 0;
            }
        }
        this.drawState();
    }

    public GoRules.BoardPosition getTurn(int playerId) {
        ImageIcon currentPlayerIcon = (playerId == 1) ? this.blackStoneIcon : this.whiteStoneIcon;
        if (this.playerPassed) {
            this.playerPassed = false;
            return null;
        }
        return this.moveButtonBoard.getMovePosition(playerId, currentPlayerIcon);
    }
}
