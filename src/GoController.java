import sun.rmi.runtime.Log;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

/**
 * Created by theo on 3/8/2016.
 */
public class GoController {

    public static int[][] gameState;
    private static final int boardSize = 9;
    private static LinkedList<Index> lifo = new LinkedList<>();
    private static Vector<Index> discovered = new Vector<>();
    private static int rowMin, rowMax, colMin, colMax;

    public static GoUI ui = new GoUI(boardSize);

    public GoController() {

    }

    public static void updateGameState(int[][] state) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                gameState[i][j] = state[i][j];
            }
        }
    }

    public static Vector<Index> getAdjacent(Index i, int id) {
        updateGameState(ui.getGameState());
        Vector<Index> adjacentIntersections = new Vector<>();
        if ((i.x - 1) >= 0 && gameState[i.x - 1][i.y] == id) {
            adjacentIntersections.add(new Index(i.x - 1, i.y));
        }

        if ((i.x + 1) < boardSize && gameState[i.x + 1][i.y] == id) {
            adjacentIntersections.add(new Index(i.x + 1, i.y));
        }

        if ((i.y + 1) < boardSize && gameState[i.x][i.y + 1] == id) {
            adjacentIntersections.add(new Index(i.x, i.y + 1));
        }

        if ((i.y - 1) >= 0 && gameState[i.x][i.y - 1] == id) {
            adjacentIntersections.add(new Index(i.x, i.y - 1));
        }

        if ((i.x - 1) >= 0 && (gameState[i.x - 1][i.y] == id || gameState[i.x - 1][i.y] == 0)) {

            if ((i.y + 1) < boardSize && gameState[i.x - 1][i.y + 1] == id) {
                adjacentIntersections.add(new Index(i.x - 1, i.y + 1));
            }
        } else if ((i.y + 1) < boardSize && (gameState[i.x][i.y + 1] == id || gameState[i.x][i.y + 1] == 0)) {
            if ((i.x - 1) >= 0 && gameState[i.x - 1][i.y + 1] == id) {
                adjacentIntersections.add(new Index(i.x - 1, i.y + 1));
            }
        }

        if ((i.x + 1) < boardSize && (gameState[i.x + 1][i.y] == id || gameState[i.x + 1][i.y] == 0)) {
            if ((i.y + 1) < boardSize && gameState[i.x + 1][i.y + 1] == id) {
                adjacentIntersections.add(new Index(i.x + 1, i.y + 1));
            }
        } else if ((i.y + 1) < boardSize && (gameState[i.x][i.y + 1] == id || gameState[i.x][i.y + 1] == 0)) {
            if ((i.x + 1) < boardSize && gameState[i.x + 1][i.y + 1] == id) {
                adjacentIntersections.add(new Index(i.x + 1, i.y + 1));
            }
        }
        if ((i.x - 1) >= 0 && (gameState[i.x - 1][i.y] == id || gameState[i.x - 1][i.y] == 0)) {
            if ((i.y - 1) >= 0 && gameState[i.x - 1][i.y - 1] == id) {
                adjacentIntersections.add(new Index(i.x - 1, i.y - 1));
            }
        } else if ((i.y - 1) >= 0 && (gameState[i.x][i.y - 1] == id || gameState[i.x][i.y - 1] == 0)) {
            if ((i.x - 1) >= 0 && gameState[i.x - 1][i.y - 1] == id) {
                adjacentIntersections.add(new Index(i.x - 1, i.y - 1));
            }
        }

        if ((i.y - 1) >= 0 && (gameState[i.x][i.y - 1] == id || gameState[i.x][i.y - 1] == 0)) {
            if ((i.x + 1) < boardSize && gameState[i.x + 1][i.y - 1] == id) {
                adjacentIntersections.add(new Index(i.x + 1, i.y - 1));
            }
        } else if ((i.x + 1) < boardSize && (gameState[i.x + 1][i.y] == id || gameState[i.x + 1][i.y] == 0)) {
            if ((i.y - 1) >= 0 && gameState[i.x + 1][i.y - 1] == id) {
                adjacentIntersections.add(new Index(i.x + 1, i.y - 1));
            }
        }

        return adjacentIntersections;
    }

    public static void clearArea(int rowMin, int rowMax, int colMin, int colMax, int id) {
        for (int i = rowMin + 1; i < rowMax; i++) {
            for (int j = colMin + 1; j < colMax; j++) {
                if (gameState[i][j] == id) {
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                }
            }
        }
    }

    public static void clearAreaMod(int rowMin, int rowMax, int colMin, int colMax, int id) {
       if(rowMin == 0 && colMin == 0) {
           for (int i = rowMin; i < rowMax; i++) {
               for (int j = colMin; j < colMax; j++) {
                   if (gameState[i][j] == id) {
                       gameState[i][j] = 0;
                       ui.setGameState(gameState);
                   }
               }
           }
       }
       else if(rowMin == 0 && colMin != 0 && colMax != boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }
        else if(rowMin == 0 && colMax == boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }
       else if(colMax == boardSize - 1 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }
       else if(colMax == boardSize - 1 && rowMax == boardSize - 1) {
            for (int i = rowMin + 1; i <= rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }
       else if(rowMax == boardSize - 1 && colMax != boardSize - 1 && colMin != 0) {
            for (int i = rowMin; i <= rowMax; i++) {
                for (int j = colMin + 1; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }
       else if(colMin == 0 && rowMax == boardSize - 1) {
            for (int i = rowMin + 1; i <= rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }
       else if(colMin == 0 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int i = rowMin + 1; i < rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
       }

    }

    private static boolean isIn(Vector<Index> v, Index i) {
        for (Index ind : v) {
            if (ind.x == i.x && ind.y == i.y) {
                return true;
            }
        }

        return false;
    }

    public static boolean dfs(Index node, Index root, int id) {
        if (!(node.x == root.x && node.y == root.y)) {
            discovered.add(node);
        }
        else if(discovered.size() > 1){
            //System.out.println("Found cycle");
            return true;
        }
        if (!getAdjacent(node, id).isEmpty()) {
            for (Index i : getAdjacent(node, id)) {
                if (rowMin > i.x) {
                    rowMin = i.x;
                }
                if (rowMax < i.x) {
                    rowMax = i.x;
                }

                if (colMin > i.y) {
                    colMin = i.y;
                }
                if (colMax < i.y) {
                    colMax = i.y;
                }
                if (!isIn(discovered, i)) {
                    //System.out.println("Discovered :" + i.x + " " + i.y);
                    return dfs(i, root, id);
                }
            }
        }
        return false;
    }

    public static boolean detectCycle(int rootRow, int rootCol, int id) {
        Index root = new Index(rootRow, rootCol);
        return dfs(root, root, id);
    }

    public static boolean dfsMod(Index node, Index root, int id) {
        discovered.add(node);
        if (discovered.size() > 1 && (((root.y - 1) < 0 && (node.y - 1) < 0) || ((root.x - 1) < 0 && (node.x - 1) < 0)
                || ((root.y + 1) >= boardSize && (node.y + 1) >= boardSize)
                || ((root.x + 1) >= boardSize && (node.x + 1) >= boardSize)
                || ((root.y - 1) < 0 && (node.x - 1) < 0)
                || ((root.x - 1) < 0 && (node.y + 1) >= boardSize)
                || ((root.y + 1) >= boardSize && (node.x + 1) >= boardSize)
                || ((root.x + 1) >= boardSize && (node.y - 1) < 0))) {
            System.out.println(root.x + " " + root.y + " - " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
            return true;
        }
        if (!getAdjacent(node, id).isEmpty()) {
            for (Index i : getAdjacent(node, id)) {
                if (rowMin > i.x) {
                    rowMin = i.x;
                }
                if (rowMax < i.x) {
                    rowMax = i.x;
                }

                if (colMin > i.y) {
                    colMin = i.y;
                }
                if (colMax < i.y) {
                    colMax = i.y;
                }
                if (!isIn(discovered, i)) {
                    //System.out.println("Discovered :" + i.x + " " + i.y);
                    return dfsMod(i, root, id);
                }
            }
        }
        return false;
    }

    public static boolean detectOpenCycle(int rootRow, int rootCol, int id) {
        Index root = new Index(rootRow, rootCol);
        return dfsMod(root, root, id);
    }

    public static void captureCheck(int id) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gameState[i][j] == id) {
                    //System.out.println("New element");
                    rowMin = boardSize + 1;
                    rowMax = -1;
                    colMin = boardSize + 1;
                    colMax = -1;
                    discovered.clear();
                    if (detectCycle(i, j, id)) {
                        boolean checkFull = true;

                        for (int k = rowMin + 1; k < rowMax; k++) {
                            for (int l = colMin + 1; l < colMax; l++) {
                                if (gameState[k][l] == 0) {
                                    checkFull = false;
                                }
                            }
                        }
                        if (checkFull) {
                            //System.out.println(rowMin + " " + rowMax + " " + colMin + " " + colMax);
                            if(id == 1) {
                                clearArea(rowMin, rowMax, colMin, colMax, 2);
                            }
                            else clearArea(rowMin, rowMax, colMin, colMax, 1);
                        }
                    }

                    rowMin = boardSize + 1;
                    rowMax = -1;
                    colMin = boardSize + 1;
                    colMax = -1;
                    discovered.clear();
                    if (detectOpenCycle(i, j, id)) {
                        boolean checkFullMod = true;

                        if(rowMin == 0 && colMin == 0) {
                            for (int k = rowMin; k < rowMax; k++) {
                                for (int l = colMin; l < colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(rowMin == 0 && colMin != 0 && colMax != boardSize - 1) {
                            for (int k = rowMin; k < rowMax; k++) {
                                for (int l = colMin + 1; l < colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(rowMin == 0 && colMax == boardSize - 1) {
                            for (int k = rowMin; k < rowMax; k++) {
                                for (int l = colMin + 1; l <= colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(colMax == boardSize - 1 && rowMin != 0 && rowMax != boardSize - 1) {
                            for (int k = rowMin; k < rowMax; k++) {
                                for (int l = colMin + 1; l <= colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(colMax == boardSize - 1 && rowMax == boardSize - 1) {
                            for (int k = rowMin + 1; k <= rowMax; k++) {
                                for (int l = colMin + 1; l <= colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(rowMax == boardSize - 1 && colMax != boardSize - 1 && colMin != 0) {
                            for (int k = rowMin; k <= rowMax; k++) {
                                for (int l = colMin + 1; l < colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(colMin == 0 && rowMax == boardSize - 1) {
                            for (int k = rowMin + 1; k <= rowMax; k++) {
                                for (int l = colMin; l < colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        else if(colMin == 0 && rowMin != 0 && rowMax != boardSize - 1) {
                            for (int k = rowMin + 1; k < rowMax; k++) {
                                for (int l = colMin; l < colMax; l++) {
                                    if (gameState[k][l] == 0) {
                                       checkFullMod = false;
                                    }
                                }
                            }
                        }
                        if (checkFullMod) {
                            //System.out.println(rowMin + " " + rowMax + " " + colMin + " " + colMax);
                            if(id == 1) {
                                clearAreaMod(rowMin, rowMax, colMin, colMax, 2);
                            }
                            else clearAreaMod(rowMin, rowMax, colMin, colMax, 1);
                        }
                    }
                }
            }
        }
    }

    public static void main(String args[]) {
        GoController controller = new GoController();

        gameState = new int[boardSize][boardSize];
        Random rn = new Random();


        //for (int i = 0; i < boardSize; i++) {
        //    for (int j = 0; j < boardSize; j++) {
        //        gameState[i][j] = rn.nextInt(3);
        //    }
        //}

        gameState[3][2] = 2;
        gameState[4][1] = 2;
        gameState[5][2] = 2;
        gameState[5][3] = 2;
        gameState[6][4] = 2;
        gameState[5][5] = 2;
        gameState[8][6] = 2;
        gameState[3][5] = 2;
        gameState[3][4] = 2;
        gameState[3][3] = 2;
        gameState[6][5] = 2;
        gameState[7][5] = 2;
        gameState[4][2] = 1;
        gameState[4][3] = 1;
        gameState[4][4] = 1;
        gameState[4][5] = 1;
        gameState[5][4] = 1;
        gameState[2][4] = 1;
        gameState[1][5] = 1;
        gameState[7][6] = 2;
        gameState[1][5] = 2;
        gameState[1][7] = 2;
        gameState[2][6] = 2;


        ui.setGameState(gameState);


        //clearArea(3, 6, 1, 6, 2);



        while (true) {
            //updateGameState(ui.getGameState());
            ui.getTurn(1);
            captureCheck(1);
            captureCheck(2);
            ui.getTurn(2);
            captureCheck(1);
            captureCheck(2);
        }
    }
}
