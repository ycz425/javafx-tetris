import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class Tetromino {

    private int x; //grid coords
    private int y;
    private ArrayList<Piece> list;
    private Color color;

    public Tetromino(Color color, int x, int y, Piece p1, Piece p2, Piece p3){ //define 3 non-center pieces
        this.color = color;
        this.x = x;
        this.y = y;
        list = new ArrayList<>();
        list.add(new Piece(0, 0));
        list.add(p1);
        list.add(p2);
        list.add(p3);
    }

    public Tetromino(Color color, int x, int y, ArrayList<Piece> list){ //used for duplicate
        this.color = color;
        this.x = x;
        this.y = y;
        this.list = new ArrayList<>();
        for (Piece p : list){
            this.list.add(new Piece(p.getX(), p.getY())); //add parent parameter if needed
        }
    }

    public Tetromino(Color color, int x, int y){ //for detached piece
        this.color = color;
        this.x = x;
        this.y = y;
        this.list = new ArrayList<>();
        list.add(new Piece(0, 0));
    }

    public Tetromino duplicate(){
        return new Tetromino(color, x, y, list);
    }

    public void draw(Group root){
        for(Piece p : list) {
            Rectangle tile = new Rectangle();
            tile.setFill(color);
            tile.setHeight(30);
            tile.setWidth(30);
            tile.setX(x * 30 + p.getX() * 30 + 200);
            tile.setY(y * 30 + p.getY() * 30);

            root.getChildren().add(tile);
        }
    }

    public void move(int x, int y){ //grid coords
        this.x += x;
        this.y += y;
    }

    public void rotate(){
        for (Piece p : list){
            if (p.getX() == 1 && p.getY() == 0)
                p.setXY(0, 1);
            else if (p.getX() == 0 && p.getY() == 1)
                p.setXY(-1, 0);
            else if (p.getX() == -1 && p.getY() == 0)
                p.setXY(0, -1);
            else if (p.getX() == 0 && p.getY() == -1)
                p.setXY(1, 0);

            if (p.getX() == 2 && p.getY() == 0)
                p.setXY(0, 2);
            else if (p.getX() == 0 && p.getY() == 2)
                p.setXY(-2, 0);
            else if (p.getX() == -2 && p.getY() == 0)
                p.setXY(0, -2);
            else if (p.getX() == 0 && p.getY() == -2)
                p.setXY(2, 0);

            if (p.getX() == 1 && p.getY() == -1)
                p.setXY(1, 1);
            else if (p.getX() == 1 && p.getY() == 1)
                p.setXY(-1, 1);
            else if (p.getX() == -1 && p.getY() == 1)
                p.setXY(-1, -1);
            else if (p.getX() == -1 && p.getY() == -1)
                p.setXY(1, -1);
        }
    }

    public ArrayList<Tetromino> disassemble(){ //removes piece at index
        ArrayList<Tetromino> newT = new ArrayList<>();
        for (Piece p : list)
            newT.add(new Tetromino(color, x + p.getX(), y + p.getY()));

        return newT;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Color getColor(){
        return color;
    }

//    public int xCoord(){
//        return x * 30;
//    }
//
//    public int yCoord(){
//        return y * 30;
//    }

    public ArrayList<Piece> getList(){
        return list;
    }
}
