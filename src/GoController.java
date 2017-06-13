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

    public static boolean isInCycle(int row, int col, Vector<Index> cycleNodes) {
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
                //System.out.println(row + " " + col + " " + "ROWDEC " + rowDec);
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
// Removed condition to be between min and max
            if (!changeHappened) {
               if (isIn(cycleNodes, new Index(row, col)) || !(isIn(cycleNodes, new Index(rowDec, col))
                       && isIn(cycleNodes, new Index(rowInc, col))
                       && isIn(cycleNodes, new Index(row, colDec))
                       && isIn(cycleNodes, new Index(row, colInc))))
                   return false;
               else return true;
            }
        }
    }

    public static boolean isInCycleMod(int row, int col, Vector<Index> cycleNodes) {
        int rowDec, rowInc, colDec, colInc;
        boolean changeHappened = false;
        rowDec = row;
        rowInc = row;
        colDec = col;
        colInc = col;

        while (true) {
            changeHappened = false;
            if (!(isIn(cycleNodes, new Index(rowDec, col)) || rowDec == 0)) {
                rowDec--;
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
            if (changeHappened == false) break;
        }


            //System.out.println("Element : " + row + "," + col + " " + rowDec + " " + rowInc + " " + colDec + " " + colInc);
            if (!changeHappened && rowMin == 0 && colMax == (boardSize - 1)) {
                if (isIn(cycleNodes, new Index(row, col)) || !((isIn(cycleNodes, new Index(rowDec, col)) || rowDec == 0)
                        && isIn(cycleNodes, new Index(rowInc, col))
                        && isIn(cycleNodes, new Index(row, colDec))
                        && (isIn(cycleNodes, new Index(row, colInc)) || colInc == (boardSize - 1)))) {
                    return false;
                }
            } else if (!changeHappened && rowMax == (boardSize - 1) && colMax == (boardSize - 1)) {
                if (isIn(cycleNodes, new Index(row, col)) || !(isIn(cycleNodes, new Index(rowDec, col))
                        && (isIn(cycleNodes, new Index(rowInc, col)) || rowInc == (boardSize - 1))
                        && isIn(cycleNodes, new Index(row, colDec))
                        && (isIn(cycleNodes, new Index(row, colInc)) || colInc == (boardSize - 1)))) {
                    return false;
                }
            } else if (!changeHappened && rowMax == (boardSize - 1) && colMin == 0) {
                if (isIn(cycleNodes, new Index(row, col)) || !(isIn(cycleNodes, new Index(rowDec, col))
                        && (isIn(cycleNodes, new Index(rowInc, col)) || rowInc == (boardSize - 1))
                        && (isIn(cycleNodes, new Index(row, colDec)) || colDec == 0)
                        && isIn(cycleNodes, new Index(row, colInc)))) {
                    return false;
                }
            } else if (!changeHappened && rowMin == 0 || colMin == 0) {
                if (isIn(cycleNodes, new Index(row, col)) && !((isIn(cycleNodes, new Index(rowDec, col)) || rowDec == 0)
                        && isIn(cycleNodes, new Index(rowInc, col))
                        && (isIn(cycleNodes, new Index(row, colDec)) || colDec == 0)
                        && isIn(cycleNodes, new Index(row, colInc)))) {
                    return false;

                }
            } else if (!changeHappened && rowMin == 0) {
                if (isIn(cycleNodes, new Index(row, col)) || !((isIn(cycleNodes, new Index(rowDec, col)) || rowDec == 0)
                        && isIn(cycleNodes, new Index(rowInc, col))
                        && isIn(cycleNodes, new Index(row, colDec))
                        && isIn(cycleNodes, new Index(row, colInc)))) {
                    return false;

                }
            } else if (!changeHappened && colMax == (boardSize - 1)) {
                if (isIn(cycleNodes, new Index(row, col)) || !(isIn(cycleNodes, new Index(rowDec, col))
                        && isIn(cycleNodes, new Index(rowInc, col))
                        && isIn(cycleNodes, new Index(row, colDec))
                        && (isIn(cycleNodes, new Index(row, colInc)) || colInc == (boardSize - 1)))) {
                    return false;

                }
            } else if (!changeHappened && rowMax == (boardSize - 1)) {
                if (isIn(cycleNodes, new Index(row, col)) || !(isIn(cycleNodes, new Index(rowDec, col))
                        && (isIn(cycleNodes, new Index(rowInc, col)) || rowInc == (boardSize - 1))
                        && isIn(cycleNodes, new Index(row, colDec))
                        && isIn(cycleNodes, new Index(row, colInc)))) {
                    System.out.println("HEY");
                    return false;

                }
            } else if (!changeHappened && colMin == 0) {
                if (isIn(cycleNodes, new Index(row, col)) || !(isIn(cycleNodes, new Index(rowDec, col))
                        && isIn(cycleNodes, new Index(rowInc, col))
                        && (isIn(cycleNodes, new Index(row, colDec)) || colDec == 0)
                        && isIn(cycleNodes, new Index(row, colInc)))) {
                    return false;

                }
            } else return true;
    return true;
    }

    public static void clearArea(int rowMin, int rowMax, int colMin, int colMax, int id) {
        System.out.println("AREA CLEARED : " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
        for (int i = rowMin + 1; i < rowMax; i++) {
            for (int j = colMin + 1; j < colMax; j++) {
                if (gameState[i][j] == id && isInCycle(i, j, cycleNodes)) {
                    gameState[i][j] = 0;
                    ui.setGameState(gameState);
                }
            }
        }
    }

    public static void clearAreaMod(int rowMin, int rowMax, int colMin, int colMax, int id) {
        System.out.println("AREA MOD CLEARED : " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
        if (rowMin == 0 && colMin == 0) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (rowMin == 0 && colMin != 0 && colMax != boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j < colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (rowMin == 0 && colMax == boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int i = rowMin; i < rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMax == boardSize - 1) {
            for (int i = rowMin + 1; i <= rowMax; i++) {
                for (int j = colMin + 1; j <= colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (rowMax == boardSize - 1 && colMax != boardSize - 1 && colMin != 0) {
            for (int i = rowMin; i <= rowMax; i++) {
                for (int j = colMin + 1; j < colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMin == 0 && rowMax == boardSize - 1) {
            for (int i = rowMin + 1; i <= rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        } else if (colMin == 0 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int i = rowMin + 1; i < rowMax; i++) {
                for (int j = colMin; j < colMax; j++) {
                    if (gameState[i][j] == id && isInCycleMod(i, j, cycleNodes)) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    }
                }
            }
        }

    }

    public static boolean dfs(Index node, Index root, int id) {
        Vector<Index> temp = new Vector<>();
        if (!(node.x == root.x && node.y == root.y)) {
            if (!isIn(discovered, node)) {
                discovered.add(node);
            }
            if (!isIn(cycleNodes, node)) {
                //System.out.println("Cycle: " + node.x + "," + node.y);
                cycleNodes.add(node);
            }
        } else if (cycleNodes.size() > 2) {
            //System.out.println("Found cycle");
            cycleNodes.add(node);
            System.out.println("Cycle: " + node.x + "," + node.y);
            //System.out.println(cycleNodes.size());
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
            //System.out.println(rowMin + " " + rowMax + " " + colMin + " " + colMax);
            return true;
        }
        if (!getAdjacent(node, id).isEmpty()) {
            for (Index i : getAdjacent(node, id)) {
                if ((i.x == root.x && i.y == root.y) && cycleNodes.size() < 4) {
                    continue;
                }
                if (!isIn(discovered, i)) {
                    //System.out.println(root.x + " " + root.y + " " + "Discovered :" + i.x + " " + i.y);
                    return dfs(i, root, id);
                }
            }
        }
        if (discovered.indexOf(node) > 0 && !((root.x == 0 || root.y == 0 || root.y == (boardSize - 1) || root.x == (boardSize - 1))
                && ((node.x + 1) >= boardSize || (node.x - 1) < 0 || (node.y + 1) >= boardSize || (node.y - 1) < 0))) {
            //System.out.println("Alt :" + discovered.get(discovered.indexOf(node)).x + " " + discovered.get(discovered.indexOf(node)).y );
            //System.out.println(gameState[root.x][root.y] + " " + root.x + " " + root.y + " - " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
            //System.out.println("Back :" + discovered.get(discovered.indexOf(node) - 1).x + " " + discovered.get(discovered.indexOf(node) - 1).y );
            //System.out.println(gameState[root.x][root.y] + " " + root.x + " " + root.y + " - " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
            //System.out.println("TEST REMOVE: " + cycleNodes.get(cycleNodes.indexOf(node)).x + cycleNodes.get(cycleNodes.indexOf(node)).y);
            cycleNodes.remove(node);
            //System.out.println("Removed :" + node.x + "," + node.y);
            return dfs(discovered.get(discovered.indexOf(node) - 1), root, id);
        }
        return false;
    }

    public static boolean detectCycle(int rootRow, int rootCol, int id) {
        Index root = new Index(rootRow, rootCol);
        boolean checkCapture;
        boolean checkFull = true;
        //System.out.println(getAdjacent(root, id).size());
        rowMin = boardSize + 1;
        rowMax = -1;
        colMin = boardSize + 1;
        colMax = -1;
        discovered.clear();
        cycleNodes.clear();

        checkCapture = dfs(root, root, id);
        if (checkCapture) {
            //System.out.println("Cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
        }
        for (int k = rowMin + 1; k < rowMax; k++) {
            for (int l = colMin + 1; l < colMax; l++) {
                     if (isInCycle(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                         System.out.println("Cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }

                //System.out.println("FOUND GAP" + k + " " + l);
                //if (getAdjacent(new Index(k, l), id).isEmpty()) {
                    if (gameState[k][l] == 0 && isInCycle(k, l, cycleNodes)) {
                        checkFull = false;
                    }
                //}
            }
        }
        if (checkCapture && checkFull) return true;
        else return false;
    }

    public static boolean dfsMod(Index node, Index root, int id) {
        boolean hasUndiscoveredNeighbor = false;
        if (!isIn(discovered, node)) {
                discovered.add(node);
            }
        if (!isIn(cycleNodes, node)) {
            cycleNodes.add(node);
        }

        for (Index i : getAdjacent(node, id)) {
            if (!isIn(discovered, i)) {
                hasUndiscoveredNeighbor = true;
                //System.out.println("TRUE");
                break;
            }
        }

        if (cycleNodes.size() > 2 && !hasUndiscoveredNeighbor && discovered.size() > 1 &&
                (((root.y - 1) < 0 && (node.y - 1) < 0) || ((root.x - 1) < 0 && (node.x - 1) < 0)
                || ((root.y + 1) >= boardSize && (node.y + 1) >= boardSize)
                || ((root.x + 1) >= boardSize && (node.x + 1) >= boardSize)
                || ((root.y - 1) < 0 && (node.x - 1) < 0)
                || ((root.x - 1) < 0 && (node.y + 1) >= boardSize)
                || ((root.y + 1) >= boardSize && (node.x + 1) >= boardSize)
                || ((root.x + 1) >= boardSize && (node.y - 1) < 0))) {
            //System.out.println("Root: " + root.x + "," + root.y + "==>" + node.x + "," + node.y);
            for (Index i : cycleNodes) {
                System.out.println("Cycle: " + i.x + "," + i.y);
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
        if (checkCapture) {
            //System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
        }
        if (rowMin == 0 && colMin == 0) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin; l < colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                    } else {
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("1 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (rowMin == 0 && colMin != 0 && colMax != boardSize - 1) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin + 1; l < colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("2 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (rowMin == 0 && colMax == boardSize - 1) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin + 1; l <= colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("3 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int k = rowMin; k < rowMax; k++) {
                for (int l = colMin + 1; l <= colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("4 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (colMax == boardSize - 1 && rowMax == boardSize - 1) {
            for (int k = rowMin + 1; k <= rowMax; k++) {
                for (int l = colMin + 1; l <= colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("5 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (rowMax == boardSize - 1 && colMax != boardSize - 1 && colMin != 0) {
            for (int k = rowMin; k <= rowMax; k++) {
                for (int l = colMin + 1; l < colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("6 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (colMin == 0 && rowMax == boardSize - 1) {
            for (int k = rowMin + 1; k <= rowMax; k++) {
                for (int l = colMin; l < colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("7 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                    }
                }
            }
        } else if (colMin == 0 && rowMin != 0 && rowMax != boardSize - 1) {
            for (int k = rowMin + 1; k < rowMax; k++) {
                for (int l = colMin; l < colMax; l++) {
                    if (isInCycleMod(k, l, cycleNodes)) {
                        System.out.println(k + " " + l + " is in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    } else {
                        System.out.println(k + " " + l + " is not in cycle");
                        System.out.println("Open cycle detected: " + rowMin + " " + rowMax + " " + colMin + " " + colMax);
                    }
                    if (gameState[k][l] == 0 && isInCycleMod(k, l, cycleNodes)) {
                        checkFullMod = false;
                        //System.out.println("8 Checkfull for :" + rowMin + " " + rowMax + " " + colMin + " " + colMax + " unsuccessful on:" + k + "," + l);
                        }
                }
            }
        }
        return (checkCapture && checkFullMod);
    }

    public static void captureCheck(int id) {
        //System.out.println("New Check");
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gameState[i][j] == id) {
                    //System.out.println("New element" + i + " " + j);


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

    public static void simpleCaptureCheck(int id) {
        int op;
        op = 0;
        System.out.println("Simple capture check called");
        updateGameState(ui.getGameState());
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {

                if (id != 0) {
                    if (id == 2) {
                        op = 1;
                    } else if (id == 1) {
                       op = 2;
                    }

                    if (i > 0 && j > 0 && i < (boardSize - 1) && j < (boardSize - 1)
                            && gameState[i][j] == id && gameState[i - 1][j] == op
                            && gameState[i + 1][j] == op && gameState[i][j - 1] == op
                            && gameState[i][j + 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
                        ui.setGameState(gameState);
                    } else if (i == 0 && j > 0 && i < boardSize - 1 && j < boardSize - 1
                            && gameState[i][j] == id
                            && gameState[i + 1][j] == op && gameState[i][j - 1] == op
                            && gameState[i][j + 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
                        ui.setGameState(gameState);
                    } else if (i > 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                            && gameState[i][j] == id && gameState[i - 1][j] == op
                            && gameState[i + 1][j] == op
                            && gameState[i][j + 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
                        ui.setGameState(gameState);
                    } else if (i > 0 && j > 0 && i == boardSize - 1 && j < boardSize - 1
                            && gameState[i][j] == id && gameState[i - 1][j] == op
                            && gameState[i][j - 1] == op
                            && gameState[i][j + 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
                        ui.setGameState(gameState);
                    } else if (i > 0 && j > 0 && i < boardSize - 1 && j == boardSize - 1
                            && gameState[i][j] == id && gameState[i - 1][j] == op
                            && gameState[i + 1][j] == op && gameState[i][j - 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
                        ui.setGameState(gameState);
                    } else if (i == 0 && j > 0 && i < boardSize - 1 && j == boardSize - 1
                            && gameState[i][j] == id
                            && gameState[i + 1][j] == op && gameState[i][j - 1] == op) {
                        System.out.println("1: " + i + " " + j);
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                        System.out.println("Simple capture check" + i + " " + j);
                    } else if (i > 0 && j > 0 && i == boardSize - 1 && j == boardSize - 1
                            && gameState[i][j] == id && gameState[i - 1][j] == op
                            && gameState[i][j - 1] == op) {
                        gameState[i][j] = 0;
                        ui.setGameState(gameState);
                    } else if (i > 0 && j == 0 && i == boardSize - 1 && j < boardSize - 1
                            && gameState[i][j] == id && gameState[i - 1][j] == op
                            && gameState[i][j + 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
                        ui.setGameState(gameState);
                    } else if (i == 0 && j == 0 && i < boardSize - 1 && j < boardSize - 1
                            && gameState[i][j] == id
                            && gameState[i + 1][j] == op
                            && gameState[i][j + 1] == op) {
                        gameState[i][j] = 0;
                        System.out.println("Simple capture check" + i + " " + j);
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


    public static void main(String args[]) {
        Controller controller = new Controller();
        GoAI computer = new GoAI(controller, 2, 3);

        gameState = new int[boardSize][boardSize];
        Random rn = new Random();


        //for (int i = 0; i < boardSize; i++) {
        //    for (int j = 0; j < boardSize; j++) {
        //        gameState[i][j] = rn.nextInt(3);
        //    }
        //}
        gameState[0][0] = 0;
        gameState[2][1] = 0;
        gameState[3][2] = 2;
        gameState[2][3] = 0;
        gameState[1][2] = 0;
        gameState[2][2] = 0;


        ui.setGameState(gameState);


        //clearArea(3, 6, 1, 6, 2);
/*

        computer.getMove();

        while (true) {
            Index blackTurn, whiteTurn;
            boolean blackCheck = false;
            boolean whiteCheck = false;
            blackTurn = ui.getTurn(1);
            //System.out.println(blackTurn.x + " " + blackTurn.y);
            if (!((detectOpenCycle(blackTurn.x, blackTurn.y, 1)) || (detectCycle(blackTurn.x, blackTurn.y, 1)))) {
                for (int i = 0; i < boardSize; i++) {
                    for (int j = 0; j < boardSize; j++) {
                        if (gameState[i][j] == 2) {
                            if ((detectCycle(i, j, 2) && isInCycle(blackTurn.x, blackTurn.y, cycleNodes)) || (detectOpenCycle(i, j, 2) && isInCycleMod(blackTurn.x, blackTurn.y, cycleNodes))) {
                                blackCheck = true;
                                System.out.println(cycleNodes.size());
                            }
                        }
                    }
                }
            }
            if(ui.checkSelfCapture(blackTurn)&& !(detectOpenCycle(blackTurn.x, blackTurn.y, 1)) && !(detectCycle(blackTurn.x, blackTurn.y, 1))
                                            && !(((blackTurn.x - 1) >= 0 && ui.checkSelfCapture(new Index(blackTurn.x - 1, blackTurn.y)))
                                            || ((blackTurn.y + 1) < boardSize && ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y + 1)))
                                            || ((blackTurn.x + 1) < boardSize && ui.checkSelfCapture(new Index(blackTurn.x + 1, blackTurn.y)))
                                            || ((blackTurn.y - 1) >= 0 && ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y - 1)))))
            {
                blackCheck = true;
            }
            while (blackCheck) {
                blackCheck = false;
                gameState[blackTurn.x][blackTurn.y] = 0;
                ui.setGameState(gameState);
                blackTurn = ui.getTurn(1);
                if (!((detectOpenCycle(blackTurn.x, blackTurn.y, 1)) || (detectCycle(blackTurn.x, blackTurn.y, 1)))) {
                    for (int i = 0; i < boardSize; i++) {
                        for (int j = 0; j < boardSize; j++) {
                            if (gameState[i][j] == 2) {
                                if ((detectCycle(i, j, 2) && isInCycle(blackTurn.x, blackTurn.y, cycleNodes)) || (detectOpenCycle(i, j, 2) && isInCycleMod(blackTurn.x, blackTurn.y, cycleNodes))) {
                                    blackCheck = true;
                                    System.out.println(cycleNodes.size());
                                }
                            }
                        }
                    }
                }
                if(ui.checkSelfCapture(blackTurn)&& !(detectOpenCycle(blackTurn.x, blackTurn.y, 1)) && !(detectCycle(blackTurn.x, blackTurn.y, 1))
                                            && !(((blackTurn.x - 1) >= 0 && ui.checkSelfCapture(new Index(blackTurn.x - 1, blackTurn.y)))
                                            || ((blackTurn.y + 1) < boardSize && ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y + 1)))
                                            || ((blackTurn.x + 1) < boardSize && ui.checkSelfCapture(new Index(blackTurn.x + 1, blackTurn.y)))
                                            || ((blackTurn.y - 1) >= 0 && ui.checkSelfCapture(new Index(blackTurn.x, blackTurn.y - 1)))))
                {
                    blackCheck = true;
                }
            }

            captureCheck(1);
            simpleCaptureCheck(2);
            captureCheck(2);
            simpleCaptureCheck(1);

            //whiteTurn = ui.getTurn(2);
            System.out.println("Getting turn.");
            gameState[whiteTurn.getX()][whiteTurn.gety()] = 2;
            ui.setGameState(gameState);
            System.out.println(whiteTurn.getX() + ", " + whiteTurn.gety());

            if (!((detectOpenCycle(whiteTurn.x, whiteTurn.y, 2)) || (detectCycle(whiteTurn.x, whiteTurn.y, 2)))) {
                for (int i = 0; i < boardSize; i++) {
                    for (int j = 0; j < boardSize; j++) {
                        if (gameState[i][j] == 1) {
                            if ((detectCycle(i, j, 1) && isInCycle(whiteTurn.x, whiteTurn.y, cycleNodes)) || (detectOpenCycle(i, j, 1) && isInCycleMod(whiteTurn.x, whiteTurn.y, cycleNodes))) {
                                whiteCheck = true;
                                System.out.println(cycleNodes.size());
                            }
                        }
                    }
                }
            }
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
                if (!((detectOpenCycle(whiteTurn.x, whiteTurn.y, 2)) || (detectCycle(whiteTurn.x, whiteTurn.y, 2)))) {
                    for (int i = 0; i < boardSize; i++) {
                        for (int j = 0; j < boardSize; j++) {
                            if (gameState[i][j] == 1) {
                                if ((detectCycle(i, j, 1) && isInCycle(whiteTurn.x, whiteTurn.y, cycleNodes)) || (detectOpenCycle(i, j, 1) && isInCycleMod(whiteTurn.x, whiteTurn.y, cycleNodes))) {
                                    whiteCheck = true;
                                    System.out.println(cycleNodes.size());
                                }
                            }
                        }
                    }
                }
                if(ui.checkSelfCapture(whiteTurn) && !(detectOpenCycle(whiteTurn.x, whiteTurn.y, 2)) && !(detectCycle(whiteTurn.x, whiteTurn.y, 2))
                                            && !(ui.checkSelfCapture(new Index(whiteTurn.x - 1, whiteTurn.y))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x, whiteTurn.y + 1))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x + 1, whiteTurn.y))
                                            || ui.checkSelfCapture(new Index(whiteTurn.x, whiteTurn.y - 1))))
                {
                    whiteCheck = true;
                }

            }
            captureCheck(2);
            simpleCaptureCheck(1);
            captureCheck(1);
            simpleCaptureCheck(2);
        }
        */
    }
}
