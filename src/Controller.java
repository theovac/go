/**
 * Created by theovac on 23/5/2017.
 */
public class Controller {
    int [][] gameState;
    public Controller() {
    }

    public static void main(String args[]) {
        Controller controller = new Controller();
        GoUI ui = new GoUI(9);
        GoRules rules = new GoRules();
        GoAI ai = new GoAI(controller, 2, 2);
        controller.gameState = ui.getGameState();
        controller.gameState[1][1] = 1;
        controller.gameState[2][0] = 1;
        controller.gameState[3][0] = 1;
        controller.gameState[2][2] = 1;
        controller.gameState[3][2] = 1;
        controller.gameState[4][1] = 1;
        controller.gameState[3][1] = 2;
        controller.gameState[2][1] = 2;
        ui.setGameState(controller.gameState);

        rules.stoneCapture(controller.gameState);
        ui.setGameState(controller.gameState);
        while(true) {
            ui.getTurn(1);
            rules.stoneCapture(controller.gameState);
            ui.setGameState(controller.gameState);
            GoRules.BoardPosition move = ai.getMove();
            controller.gameState[move.getRow()][move.getCol()] = 2;
            rules.stoneCapture(controller.gameState);
            ui.setGameState(controller.gameState);
        }
    }
}
