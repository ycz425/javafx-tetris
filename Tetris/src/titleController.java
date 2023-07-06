import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class titleController {

    public void start(ActionEvent event){
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        Game game = new Game();
        game.startGame(stage);
    }

}
