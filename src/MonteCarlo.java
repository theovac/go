import sun.reflect.generics.tree.Tree;

import java.util.*;
import java.util.function.BinaryOperator;

public class MonteCarlo {
    private static int toPlayColor, lastPlayColor;
    private static int pAtari = 50;
    private static int pPattern = 50;
    private List<String> patterns =
            new ArrayList<String>(Arrays.asList("XOX...???", "XO....?.?", "XO?X..?.?", "?O?X.XWWW",
                                                "X.?O.?###", "?X?W.O###", "XOO...?.?", "?XO?.?###",
                                                "?OX?..###", "?OX?.X###", "?OXX.O###"));
    public MonteCarlo(int toPlayColor) {
        this.toPlayColor = toPlayColor;
        if (toPlayColor == 1) {
           this.lastPlayColor = 2;
        } else this.lastPlayColor = 1;
    }
/*
    public class Node {
        int toPlayColor;
        Node parent;
        Set<Node> children;
        int[][] state;
        int wins, losses;

        private Node(int[][] state, int toPlayColor) {
            this.toPlayColor = toPlayColor;
            this.parent = null;
            this.children = null;
            this.state = state;
            this.wins = 0;
            this.losses = 0;
        }

    }
*/
    public class Move {
        GoRules.BoardPosition pos;
        int priority;

        public Move(GoRules.BoardPosition pos, int priority) {
            this.pos = pos;
            this.priority = priority;
        }
    }

    public List<Move> generate_move(Node node, int[][] state) {
        Random randGen = new Random();
        Set<GoRules.BoardPosition> checked = new HashSet<>();
        List<Move> moves = new ArrayList<>();
        List<GoRules.BoardPosition> skippedMoves = new ArrayList<>();

        // Generate moves that capture enemy stones.
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state.length; j++) {
               if (state[i][j] != node.toPlayColor && state[i][j] != 0 &&
                       !GoRules.findBoardPosition(checked, new GoRules.BoardPosition(i, j))) {
                   // Check if opponent stone group containing the stone on i, j is in atari.
                   GoRules.CheckCaptureResult captureResult =
                           GoRules.checkCapture(new GoRules.BoardPosition(i, j), state);
                   checked.addAll(captureResult.getStoneGroup());
                   if (captureResult.getLibertyCount() == 1) {
                       // Find the position where if a stone is placed, opponent stones will be captured.
                       for (GoRules.BoardPosition stone : captureResult.getStoneGroup()) {
                           System.out.println(stone.getRow() + " " + stone.getCol());
                           GoRules.GetConnectedResult connectedResult = GoRules.getConnected(stone, state);
                           if (connectedResult.getLibertyCount() == 1) {
                               // Play move with pAtari chance
                               if (randGen.nextInt(100) > pAtari) {
                                   moves.add(new Move(connectedResult.getLibertyPositions().get(0), 1));
                               } else {
                                   skippedMoves.add(connectedResult.getLibertyPositions().get(0));
                               }
                           }
                       }
                   }
               }
            }
        }
        checked = new HashSet<>();
        // Add an extra line to the array, that represents off board positions. This is needed for pattern matching.
        int[][] paddedState = new int[state.length+1][state.length];
        Arrays.fill(paddedState[paddedState.length-1], 3);
        int stateRow = 0;
        int stateCol = 0;

        for (int i = 0; i < paddedState.length - 1; i++) {
            for (int j = 0; j < paddedState[0].length; j++) {
                paddedState[i][j] = state[stateRow][stateCol];
                stateCol++;
            }
            stateCol = 0;
            stateRow++;
        }

        // Play move if the 3x3 block around it matches an urgency pattern.
        for (int i = 1; i < paddedState.length-1; i++) {
            for (int j = 1; j < paddedState[0].length-1; j++) {
                // The 3x3 block in string form.
                String blockString = "" + paddedState[i-1][j-1] + paddedState[i-1][j] + paddedState[i-1][j+1] +
                        paddedState[i][j-1] + paddedState[i][j] + paddedState[i][j+1] + paddedState[i+1][j-1] +
                        paddedState[i+1][j] + paddedState[i+1][j+1];
                if (matchPattern(blockString, node.toPlayColor)) {
                    if (randGen.nextInt(100) > pPattern) {
                        moves.add(new Move(new GoRules.BoardPosition(i, j), 2));
                        System.out.println("DEBUG: Found move 2");
                    } else {
                        skippedMoves.add(new GoRules.BoardPosition(i, j));
                        System.out.println("DEBUG: Skipped move 2");
                    }
                }
            }
        }

        // Play previously skipped moves.
        for (GoRules.BoardPosition skippedMove : skippedMoves) {
            moves.add(new Move(skippedMove, 3));
        }

        // Make a list of all the possible random moves.
        List<GoRules.BoardPosition> randomMoves = new ArrayList<>();
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state.length; j++) {
                // Add a random move if it is a valid one.
                if(GoRules.isValidMove(new GoRules.BoardPosition(i, j), node.toPlayColor, state)) {
                    GoRules.GetConnectedResult randomResult =
                            GoRules.getConnected(new GoRules.BoardPosition(i, j), state);
                    // If the move has only one liberty add a move in the position of the liberty instead.
                    if (randomResult.getLibertyCount() == 1) {
                        randomMoves.add(randomResult.getLibertyPositions().get(0));
                    } else {
                        randomMoves.add(new GoRules.BoardPosition(i, j));
                    }
                }
            }
        }
        // Play one of the random moves at random.
        if (!randomMoves.isEmpty()) {
            moves.add(new Move(randomMoves.get(randGen.nextInt(randomMoves.size() - 1)), 4));
        }

        // Play a pass move
        moves.add(null);

        return moves;
    }

    private boolean matchPattern(String blockString, int toPlayColor) {
        boolean isMatch;
        boolean foundMatch = false;
        if (toPlayColor == 1) {
            lastPlayColor = 2;
        } else lastPlayColor = 1;
        for (String pattern : this.patterns) {
            isMatch = true;
            // Translate pattern for the current player.
            pattern = pattern.replaceAll("X", Integer.toString(toPlayColor));
            pattern = pattern.replaceAll("O", Integer.toString(lastPlayColor));
            pattern = pattern.replaceAll("#", Integer.toString(3));
            pattern = pattern.replaceAll("\\.", Integer.toString(0));
            /*if (pattern.equals("?1?W02333")) {
                System.out.println("---");
                System.out.println(blockString);
                System.out.println(pattern);
            }*/
            for (int i = 0; i < blockString.length(); i++) {
                if (blockString.charAt(i) != pattern.charAt(i)) {
                    if (pattern.charAt(i) == '?' && blockString.charAt(i) != '3') {
                        continue;
                    } else if (pattern.charAt(i) == 'W' && (blockString.charAt(i) == (char)(48+lastPlayColor) || blockString.charAt(i) == '0')) {
                        continue;
                    }
                    isMatch = false;
                }
            }
            if (isMatch == true) {
                System.out.println(blockString);
                System.out.println(pattern);
                foundMatch = true;
                break;
            }
        }
        return foundMatch;
    }
}
