import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Game {
    private int[][] grid = new int[20][10];
    private ArrayList<Tetromino> originals;
    private ArrayList<Tetromino> placed;
    private Queue<Tetromino> queue;
    private Tetromino selected;
    private Tetromino held;
    private double time;
    private double time2; //for place delay

    private Group root;

    int score;
    int linesCleared;
    int level;
    double speed;

    private boolean usedHold;
    private boolean paused;
    private boolean playing;

    public void startGame(Stage stage){
        playing = true;
        root = new Group();
        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);

        speed = 1.0;
        level = 1;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                time += 0.017;

                if (!playing)
                    this.stop();

                if (time >= speed) {
                    update();
                    render();

                    time = 0;
                }
            }
        };

        scene.setOnKeyPressed(event -> {
            if (playing && !paused){
                switch (event.getCode()) {
                    case RIGHT:
                        makeMove(1, 0);
                        break;
                    case LEFT:
                        makeMove(-1, 0);
                        break;
                    case DOWN:
                        makeMove(0, 1);
                        break;
                    case UP:
                        if (selected.getColor() != Color.YELLOW) //squares don not rotate
                            makeRotation();
                        break;
                    case SPACE:
                        try {
                            hardDrop();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case C:
                        if (!usedHold) {
                            hold();
                            usedHold = true;
                        }
                        break;
                    default:
                }
            }
            if (playing && event.getCode() == KeyCode.ESCAPE)
                if (paused) {
                    timer.start();
                    paused = false;
                } else {
                    timer.stop();
                    paused = true;
                }

            if (playing) //for game over by hard drop (prevents rendering after hard drop game over)
                render();
        });

        queue = new ArrayDeque<>();
        originals = new ArrayList<>();
        placed = new ArrayList<>();

        originals.add(new Tetromino(Color.SKYBLUE, 0, 0, new Piece(-1, 0), new Piece(1, 0), new Piece(2, 0)));
        originals.add(new Tetromino(Color.LIMEGREEN, 0, 0, new Piece(-1, 0), new Piece(0, -1), new Piece(1, -1)));
        originals.add(new Tetromino(Color.RED, 0, 0, new Piece(-1, -1), new Piece(0, -1), new Piece(1, 0)));
        originals.add(new Tetromino(Color.ORANGE, 0, 0, new Piece(-1, 0), new Piece(1, 0), new Piece(1, -1)));
        originals.add(new Tetromino(Color.BLUE, 0, 0, new Piece(-1, -1), new Piece(-1, 0), new Piece(1, 0)));
        originals.add(new Tetromino(Color.YELLOW, 0, 0, new Piece(0, -1), new Piece(1, -1), new Piece(1, 0)));
        originals.add(new Tetromino(Color.PURPLE, 0, 0, new Piece(0, -1), new Piece(-1, 0), new Piece(1, 0)));

        queue.add(originals.get((int)(Math.random() * (7))).duplicate());
        queue.add(originals.get((int)(Math.random() * (7))).duplicate());
        queue.add(originals.get((int)(Math.random() * (7))).duplicate());

        spawn();
        render();

        timer.start();
    }

    public void update(){
        makeMove(0, 1);
    }

    public void render(){
        root.getChildren().clear();

        drawScreen();
        selected.draw(root);

        for (Tetromino t : placed){
            t.draw(root);
        }

        if (held != null) {
            held.setXY(-4, 3);
            held.draw(root);
        }

        int i = 3;
        for (Tetromino t : queue){
            t.setXY(13, i);
            t.draw(root);
            i += 3;
        }

        if (paused)
        {
            Rectangle screen = new Rectangle();
            screen.setHeight(600);
            screen.setWidth(300);
            screen.setX(200);
            screen.setFill(Color.BLACK);

            Label label = new Label("PAUSED");
            label.setTextFill(Color.WHITE);
            label.setLayoutX(300);
            label.setLayoutY(250);
            label.setFont(Font.font("Verdana", 25));

            root.getChildren().add(screen);
            root.getChildren().add(label);
        }

        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.setLayoutX(30);
        scoreLabel.setLayoutY(520);
        scoreLabel.setFont(Font.font("Verdana", 18));
        root.getChildren().add(scoreLabel);

        Label levelLabel = new Label("Level " + level);
        levelLabel.setLayoutX(30);
        levelLabel.setLayoutY(500);
        levelLabel.setFont(Font.font("Verdana", 15));
        root.getChildren().add(levelLabel);
    }

    public void drawScreen(){
        Line line = new Line(199, 0, 199, 600);
        Line line2 = new Line(501, 0, 501, 600);
        root.getChildren().add(line);
        root.getChildren().add(line2);
    }

    public void makeMove(int x, int y){
        if (x == 0 && y == 1 && offScreen(x, y)) { //offScreen bottom -> place
            startPlaceTimer();
            return;
        } else if (offScreen(x, y)) { //offScreen side -> don't place
            return;
        }

        if (x == 0 && y == 1 && colliding(x, y)) { //bottom colliding -> place
            startPlaceTimer();
            return;
        } else if (colliding(x, y)) { //side colliding -> don't place
            return;
        }

        selected.move(x, y);
    }

    public void makeRotation(){
        Tetromino duplicate = selected.duplicate();
        duplicate.setXY(selected.getX(), selected.getY());

        selected.rotate();

        if (embedded(-1, 0)){ //test sequence for left side
            selected.move(1, 0);
            if (embedded(-1, 0))
                selected.move(1, 0);
        } else if (embedded(1, 0)){ //test sequence for right side
            selected.move(-1, 0);
            if (embedded(1, 0))
                selected.move(-1, 0);
        }

        if (embedded(-1, 0) || embedded(0, -1) || embedded(1, 0) || embedded(0, 1))
            selected.move(0, -1);
        if (embedded(-1, 0) || embedded(0, -1) || embedded(1, 0) || embedded(0, 1))
            selected = duplicate;
    }

    public void hardDrop() throws IOException {
        boolean collision = offScreen(0, 1) || colliding(0, 1);
        while (!collision){
            selected.move(0, 1);
            collision = offScreen(0, 1) || colliding(0, 1);
        }

        place();
    }

    public void startPlaceTimer(){
        time2 = 0;

        AnimationTimer placer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                time2 += 0.017;

                if(time2 >= 0.3) {
                    try {
                        place();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.stop();
                }
            }
        };

        placer.start();
    }

    public void place() throws IOException { //places currently selected if conditions are met
        if (offScreen(0, 1) || colliding(0, 1)) {
            ArrayList<Tetromino> newT = selected.disassemble();
            for (Tetromino t : newT)
                placed.add(t);

            usedHold = false;

            updateGrid();
            sweep();

            for (int i = 0; i < 10; i++) //check for game over
                if (grid[1][i] == 1) {
                    gameOver();
                    return;
                }

            spawn();

        }
    }

    public void sweep(){
        int n = 0;
        boolean swept = false;
        for (int i = 0; i < 20; i++) {
            int sum = 0;
            for (int j = 0; j < 10; j++)
                sum += grid[i][j];

            if (sum == 10) { //if full row
                n++;
                linesCleared++;
                swept = true;
                for (int k = 0; k < placed.size(); k++){ //SWEEEEEP
                    Tetromino t = placed.get(k);
                    if (t.getY() == i)
                        placed.remove(k--);
                    else if (t.getY() < i)
                        t.setXY(t.getX(), t.getY() + 1);
                }
                updateGrid();

                if (linesCleared == 10){
                    level++;
                    linesCleared = 0;
                    if (speed - 0.03 > 0.25)
                        speed -= 0.03;
                }
            }
        }
        if (swept)
            score += 100 * Math.pow(2, n - 1);

        if (linesCleared == 10){
            level++;
            linesCleared = 0;
            if (speed - 0.03 > 0.25)
                speed -= 0.03;
        }
    }

    public void updateGrid(){
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 10; j++)
                grid[i][j] = 0;

        for (Tetromino t : placed)
            for (Piece p : t.getList()){
                grid[t.getY() + p.getY()][t.getX() + p.getX()]++;
            }
    }

    public boolean offScreen(int x, int y){ //check if offscreen when moved in following directions
        for (Piece p : selected.getList()){
            if (p.getX() + selected.getX() + x >= 10)
                return true;
            if (p.getX() + selected.getX() + x < 0)
                return true;
            if (p.getY() + selected.getY() + y >= 20)
                return true;
        }
        return false;
    }

    public boolean colliding(int x, int y){ //checks if colliding in following directions
        for (Piece p : selected.getList()){
            if (selected.getY() + p.getY() + y < 20 && selected.getX() + p.getX() + x < 10 && selected.getX() + p.getX() + x >= 0)
                if (grid[selected.getY() + p.getY() + y][selected.getX() + p.getX() + x] > 0){
                    return true;
                }
        }
        return false;
    }

    public boolean embedded(int x, int y){ //checks if embedded in following directions (for rotation check)
        return offScreen(0, 0) && offScreen(x, y) || colliding(0, 0) && colliding(x, y);
    }

    public void spawn(){
        queue.add(originals.get((int)(Math.random() * 7)).duplicate());
        selected = queue.poll();
        selected.setXY(4, 1);
    }

    public void hold(){
        Color selectedType = selected.getColor();

        if (held == null)
            spawn();
        else {
            selected = held;
            selected.setXY(4, 1);
        }

        if (selectedType == Color.SKYBLUE)
            held = originals.get(0).duplicate();
        if (selectedType == Color.LIMEGREEN)
            held = originals.get(1).duplicate();
        if (selectedType == Color.RED)
            held = originals.get(2).duplicate();
        if (selectedType == Color.ORANGE)
            held = originals.get(3).duplicate();
        if (selectedType == Color.BLUE)
            held = originals.get(4).duplicate();
        if (selectedType == Color.YELLOW)
            held = originals.get(5).duplicate();
        if (selectedType == Color.PURPLE)
            held = originals.get(6).duplicate();

    }

    public void reset(){
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 10; j++)
                grid[i][j] = 0;
        originals.clear();
        placed.clear();
        queue.clear();
        selected = null;
        held = null;
        time = 0;
        time2 = 0;

        root = null;

        score = 0;

        usedHold = false;
        paused = false;
        playing = false;
    }

    public void gameOver(){
        playing = false;

        Rectangle screen = new Rectangle();
        screen.setHeight(600);
        screen.setWidth(300);
        screen.setX(200);
        screen.setFill(Color.BLACK);
        screen.setOpacity(0.5);
        root.getChildren().add(screen);

        Button restart = new Button("RESTART");
        restart.setLayoutX(55);
        restart.setLayoutY(550);
        restart.setFont(Font.font("Verdana", 15));
        root.getChildren().add(restart);

        restart.setOnAction(event -> {
            reset();
            startGame((Stage)((Node)event.getSource()).getScene().getWindow());
        });
    }
}
