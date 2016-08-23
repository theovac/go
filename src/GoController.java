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
    private static Vector<Index> discovered = new Vector<>();
    private static Vector<Index> cycleNodes = new Vector<>();
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
        if ((i.y + 1) < boardSize && (i.x - 1) >= 0 && gameState[i.x - 1][i.y + 1] == id) {
            adjacentIntersections.add(new Index(i.x - 1, i.y + 1));
        }
        if ((i.y + 1) < boardSize && (i.x + 1) < boardSize && gameState[i.x + 1][i.y + 1] == id) {
            adjacentIntersections.add(new Index(i.x + 1, i.y + 1));
        }
        if ((i.y - 1) >= 0 && (i.x - 1) >= 0 && gameState[i.x - 1][i.y - 1] == id) {
            adjacentIntersections.add(new Index(i.x - 1, i.y - 1));
        }

        if ((i.x + 1) < boardSize && (i.y - 1) >= 0 && gameState[i.x + 1][i.y - 1] == id) {
            adjacentIntersections.add(new Index(i.x + 1, i.y - 1));
        }
        return adjacentIntersections;
    }

    public static boolean isInCycle(int row, int col, Vector<Index> cycleNodes, int id) {
        int rowDec, rowInc, colDec, colInc;
        boolean changeHappened = false;
        rowDec = row;
        rowInc = row;
        colDec = col;
        colInc = col;

        while(true) {
            changeHappened = false;
            if (!(isIn(cycleNodes, new Index(rowDec, col)) || rowDec == 0)) {
                rowDec--;
                System.out.println(row + " " + col + " " + "ROWDEC " + rowDec);
                changeHappened = true;
            }
            if (!(isIn(cycleNodes, new Index(rowInc, col)) || rowInc == (boardSize - 1))) {
                rowInc++;
                changeHappened = true;
            }
            if (!(isIn(cycleNodes, new Index(row, colDec)) || colDec == 0)) {
                colDec--;
                changeHappened = true;
            }
            if (!(isIn(cycleNodes, new Index(row, colInc)) || colInc == (boardSize - 1))) {
                colInc++;
                changeHappened = true;
            }

            if (!changeHappened) {
               if (!(rowDec >= rowMin && rowDec <= rowMax)
                       || !(rowInc >= rowMin && rowInc <= rowMax)
                       || !(colDec >= colMin && colDec <= colMax)
                       || !(colInc >= colMin && colInc <= colMax) )
                   return false;
               else return true;
            }
        }
    }

    public static void clearArea(int rowMin, int rowMax, int colMin, int colMax, int id) {
        //System.out.println("AREA CLEARED : " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
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
        if (rowMin == 0 && colMin == 0) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (rowMin == 0 && colMin != 0 && colMax != boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (rowMin == 0 && colMax == boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMax == boardSize - 1) {
            for (int i = rowMin + 1; i <= rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (rowMax == boardSize - 1 && colMax != boardSize - 1 && colMin != 0) {
            for (int i = rowMin; i <= rowMax; i++) {
                for (int j = colMin + 1; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMin == 0 && rowMax == boardSize - 1) {
            for (int i = rowMin + 1; i <= rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMin == 0 && rowMin != 0 && rowMax != boardSize - 1) {
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
        Vector<Index> temp = new Vector<>();
        if (!(node.x == root.x && node.y == root.y)) {
            if (!isIn(discovered, node)) {
                discovered.add(node);
            }
            if (!isIn(cycleNodes, node)) {
                cycleNodes.add(node);
            }
        } else if (discovered.size() > 1) {
            //System.out.println("Found cycle");
            cycleNodes.add(node);
            System.out.println(cycleNodes.size());
            for (Index i : cycleNodes) {
                //System.out.println(root.x + " " + root.y + " " + "TEST BOUNDS: " + i.x + i.y);
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
            }
            System.out.println(rowMin + " " + rowMax + " " + colMin + " " + colMax);
            return true;
        }
        if (!getAdjacent(node, id).isEmpty()) {
            for (Index i : getAdjacent(node, id)) {

                if (!isIn(discovered, i)) {

                    //System.out.println(root.x + " " + root.y + " " + "Discovered :" + i.x + " " + i.y);
                    return dfs(i, root, id);
                }
            }
        }
        if (discovered.indexOf(node) > 0 && !((root.x == 0 || root.y == 0 || root.y == (boardSize - 1) || root.x == (boardSize - 1))
                && ((node.x + 1) >= boardSize
                || (node.x - 1) < 0
                || (node.y + 1) >= boardSize
                || (node.y - 1) < 0))) {
            //System.out.println("Alt :" + discovered.get(discovered.indexOf(node)).x + " " + discovered.get(discovered.indexOf(node)).y );
            //System.out.println(gameState[root.x][root.y] + " " + root.x + " " + root.y + " - " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
            //System.out.println("Back :" + discovered.get(discovered.indexOf(node) - 1).x + " " + discovered.get(discovered.indexOf(node) - 1).y );
            //System.out.println(gameState[root.x][root.y] + " " + root.x + " " + root.y + " - " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
            //System.out.println("TEST REMOVE: " + cycleNodes.get(cycleNodes.indexOf(node)).x + cycleNodes.get(cycleNodes.indexOf(node)).y);
            cycleNodes.remove(node);
            return dfs(discovered.get(discovered.indexOf(node) - 1), root, id);
        }
        return false;
    }

    public static boolean detectCycle(int rootRow, int rootCol, int id) {
        Index root = new Index(rootRow, rootCol);
        boolean checkCapture;
        boolean checkFull = true;

        rowMin = boardSize + 1;
        rowMax = -1;
        colMin = boardSize + 1;
        colMax = -1;
        discovered.clear();
        cycleNodes.clear();

        checkCapture = dfs(root, root, id);
        for (int k = rowMin + 1; k < rowMax; k++) {
            for (int l = colMin + 1; l < colMax; l++) {
                System.out.println("FOUND GAP" + k + " " + l);
                //if (getAdjacent(new Index(k, l), id).isEmpty()) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFull = false;
                    }
                //}
            }
        }
        if (checkCapture && checkFull) return true;
        else return false;
    }

    public static boolean dfsMod(Index node, Index root, int id) {
        discovered.add(node);
        cycleNodes.add(node);

        if (discovered.size() > 1 && (((root.y - 1) < 0 && (node.y - 1) < 0) || ((root.x - 1) < 0 && (node.x - 1) < 0)
                || ((root.y + 1) >= boardSize && (node.y + 1) >= boardSize)
                || ((root.x + 1) >= boardSize && (node.x + 1) >= boardSize)
                || ((root.y - 1) < 0 && (node.x - 1) < 0)
                || ((root.x - 1) < 0 && (node.y + 1) >= boardSize)
                || ((root.y + 1) >= boardSize && (node.x + 1) >= boardSize)
                || ((root.x + 1) >= boardSize && (node.y - 1) < 0))) {
            for (Index i : cycleNodes) {
                //System.out.println(root.x + " " + root.y + " " + "TEST BOUNDS: " + i.x + i.y);
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
            }
            //System.out.println(gameState[root.x][root.y] + " " + root.x + " " + root.y + " - " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
            return true;
        }
        if (!getAdjacent(node, id).isEmpty()) {
            for (Index i : getAdjacent(node, id)) {

                if (!isIn(discovered, i)) {
                    //System.out.println("Discovered :" + i.x + " " + i.y);
                    return dfsMod(i, root, id);
                }
            }
        }
        if (discovered.indexOf(node) > 0 && !((root.x == 0 || root.y == 0 || root.y == (boardSize - 1) || root.x == (boardSize - 1))
                && ((node.x + 1) >= boardSize
                || (node.x - 1) < 0
                || (node.y + 1) >= boardSize
                || (node.y - 1) < 0))) {
            //System.out.println("Alt :" + discovered.get(discovered.indexOf(node)).x + " " + discovered.get(discovered.indexOf(node)).y );
            //System.out.println("Back :" + discovered.get(discovered.indexOf(node) - 1).x + " " + discovered.get(discovered.indexOf(node) - 1).y );
            cycleNodes.remove(node);
            return dfs(discovered.get(discovered.indexOf(node) - 1), root, id);
        }
        return false;
    }

    public static boolean detectOpenCycle(int rootRow, int rootCol, int id) {
        Index root = new Index(rootRow, rootCol);
        boolean checkCapture;
        boolean checkFullMod = true;

        rowMin = boardSize + 1;
        rowMax = -1;
        colMin = boardSize + 1;
        colMax = -1;
        discovered.clear();
        cycleNodes.clear();

        checkCapture = dfsMod(root, root, id);
        if (rowMin == 0 && colMin == 0) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin; l < colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (rowMin == 0 && colMin != 0 && colMax != boardSize - 1) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin + 1; l < colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (rowMin == 0 && colMax == boardSize - 1) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin + 1; l <= colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin + 1; l <= colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMax == boardSize - 1) {
            for (int k = rowMin + 1; k <= rowMax; k++) {
                for (int l = colMin + 1; l <= colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (rowMax == boardSize - 1 && colMax != boardSize - 1 && colMin != 0) {
            for (int k = rowMin; k <= rowMax; k++) {
                for (int l = colMin + 1; l < colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (colMin == 0 && rowMax == boardSize - 1) {
            for (int k = rowMin + 1; k <= rowMax; k++) {
                for (int l = colMin; l < colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                    }
                }
            }
        } else if (colMin == 0 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int k = rowMin + 1; k < rowMax; k++) {
                for (int l = colMin; l < colMax; l++) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes, id)) {
                        checkFullMod = false;
                        }
                }
            }
        }
        return (checkCapture && checkFullMod);
    }

    public static void captureCheck(int id) {
        System.out.println("New Check");
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gameState[i][j] == id) {
                    System.out.println("New element" + i + " " + j);


                    if (detectCycle(i, j, id)) {
                        //System.out.println("AYYYYYYYYYYYY iT WORKED");
                        if (id == 1) {
                            clearArea(rowMin, rowMax, colMin, colMax, 2);
                        } else clearArea(rowMin, rowMax, colMin, colMax, 1);
                    } else if (detectOpenCycle(i, j, id)) {
                            //System.out.println(rowMin + " " + rowMax + " " + colMin + " " + colMax);
                        if (id == 1) {
                            clearAreaMod(rowMin, rowMax, colMin, colMax, 2);
                        } else clearAreaMod(rowMin, rowMax, colMin, colMax, 1);
                    }
                }
            }
        }
    }

    public static void simpleCaptureCheck() {
        int id, op;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                id = gameState[i][j];
                if (id == 2) {
                    op = 1;
                } else {
                    op = 2;
                }
                if (i > 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                        && gameState[i][j] == op && gameState[i - 1][j] == id
                        && gameState[i + 1][j] == id && gameState[i][j - 1] == id
                        && gameState[i][j + 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i == 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                        && gameState[i][j] == op
                        && gameState[i + 1][j] == id && gameState[i][j - 1] == id
                        && gameState[i][j + 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i > 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                        && gameState[i][j] == op && gameState[i - 1][j] == id
                        && gameState[i + 1][j] == id
                        && gameState[i][j + 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i > 0 && j > 0 && i == boardSize - 1 && j < boardSize - 1
                        && gameState[i][j] == op && gameState[i - 1][j] == id
                        && gameState[i][j - 1] == id
                        && gameState[i][j + 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i > 0 && j > 0 && i < boardSize - 1 && j == boardSize - 1
                        && gameState[i][j] == op && gameState[i - 1][j] == id
                        && gameState[i + 1][j] == id && gameState[i][j - 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i == 0 && j > 0 && i < boardSize - 1 && j == boardSize - 1
                        && gameState[i][j] == op
                        && gameState[i + 1][j] == id && gameState[i][j - 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i > 0 && j > 0 && i == boardSize - 1 && j == boardSize - 1
                        && gameState[i][j] == op && gameState[i - 1][j] == id
                        && gameState[i + 1][j] == id && gameState[i][j - 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i > 0 && j == 0 && i == boardSize - 1 && j < boardSize - 1
                        && gameState[i][j] == op && gameState[i - 1][j] == id
                        && gameState[i][j + 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                } else if (i == 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                        && gameState[i][j] == op
                        && gameState[i + 1][j] == id
                        && gameState[i][j + 1] == id) {
                    System.out.println("1: " + i + " " + j);
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
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
            Index blackTurn, whiteTurn;
            boolean blackCheck = false;
            boolean whiteCheck = false;
            blackTurn = ui.getTurn(1);
            System.out.println(blackTurn.x + " " + blackTurn.y);
            if(ui.checkSelfCapture(blackTurn)&& !(detectOpenCycle(blackTurn.x, blackTurn.y, 1)) && !(detectCycle(blackTurn.x, blackTurn.y, 1))
                                            && !(ui.checkSelfCapture(new Index(blackTurn.x - 1, blackTurn.y))
                                            || ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y + 1))
                                            || ui.checkSelfCapture(new Index(blackTurn.x + 1, blackTurn.y))
                                            || ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y - 1))))
            {
                blackCheck = true;
            }
            while (blackCheck) {
                blackCheck = false;
                gameState[blackTurn.x][blackTurn.y] = 0;
                ui.setGameState(gameState);
                blackTurn = ui.getTurn(1);
                if (ui.checkSelfCapture(blackTurn) && !(detectOpenCycle(blackTurn.x, blackTurn.y, 1)) && !(detectCycle(blackTurn.x, blackTurn.y, 1))
                        && !(ui.checkSelfCapture(new Index(blackTurn.x - 1, blackTurn.y))
                        || ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y + 1))
                        || ui.checkSelfCapture(new Index(blackTurn.x + 1, blackTurn.y))
                        || ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y - 1)))) {
                    blackCheck = true;
                }
            }
            simpleCaptureCheck();
            captureCheck(1);
            captureCheck(2);
            //captureCheck(1);
            whiteTurn = ui.getTurn(2);
            System.out.println(whiteTurn.x + " " + whiteTurn.y);
            if(ui.checkSelfCapture(whiteTurn) && !(detectOpenCycle(whiteTurn.x, whiteTurn.y, 2)) && !(detectCycle(whiteTurn.x, whiteTurn.y, 2))
                                            && !(ui.checkSelfCapture(new Index(whiteTurn.x - 1, whiteTurn.y))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x, whiteTurn.y + 1))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x + 1, whiteTurn.y))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x, whiteTurn.y - 1))))
            {
                whiteCheck = true;
            }

            while (whiteCheck) {
                whiteCheck = false;
                gameState[whiteTurn.x][whiteTurn.y] = 0;
                ui.setGameState(gameState);
                whiteTurn = ui.getTurn(2);
                if(ui.checkSelfCapture(whiteTurn) && !(detectOpenCycle(whiteTurn.x, whiteTurn.y, 2)) && !(detectCycle(whiteTurn.x, whiteTurn.y, 2))
                                            && !(ui.checkSelfCapture(new Index(whiteTurn.x - 1, whiteTurn.y))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x, whiteTurn.y + 1))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x + 1, whiteTurn.y))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x, whiteTurn.y - 1))))
                {
                    whiteCheck = true;
                }

            }
            simpleCaptureCheck();
            captureCheck(2);
            captureCheck(1);
        }
    }
}
