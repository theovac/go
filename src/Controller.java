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
        GoAI ai = new GoAI(controller, 2, 2);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                GoAI.CheckCaptureResult result = ai.checkCapture(new GoAI.BoardPosition(i, j), controller.gameState);
                System.out.println(result.getStoneGroup());
                for (GoAI.BoardPosition captured : result.getStoneGroup()) {
                    System.out.println(captured.getRow() + ", " + captured.getCol());
                    controller.gameState[captured.getRow()][captured.getCol()] = 0;
                    ui.setGameState(controller.gameState);
                }
            }
        }
    }
}
