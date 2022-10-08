import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.server.GameState;

import java.util.List;
import java.util.stream.Stream;

public class GameStateTest {
    @Test
    public void TestVerticalWin() {

        //Arrange
        GameState gameState = new GameState("PlayerA");
        gameState.setPlayerNameB("PlayerB");
        var startPlayer = gameState.getCurrentPlayer();


        //Act
        var moveResults = List.of(
                gameState.makeMove(0, 0), //1
                gameState.makeMove(1, 0), //2

                gameState.makeMove(0, 1), //1
                gameState.makeMove(1, 1), //2

                gameState.makeMove(0, 2) //1
        );

        //Assert
        Assertions.assertEquals(
                Stream.concat(
                        Stream.generate(() -> GameState.MoveResult.GameContinues).limit(moveResults.size() - 1),
                        Stream.of(GameState.MoveResult.EndYouWin)
                ).toList(),
                moveResults
        );

    }

}


