import javafx.scene.paint.Color;

public class Piece {
    private int x; //grid coords relative to center
    private int y;
//    private Tetromino parent;

    public Piece(int x, int y){
        this.x = x;
        this.y = y;
    }

//    public Piece(Tetromino parent, int x, int y){
//        this.parent = parent;
//        this.x = x;
//        this.y = y;
//    }

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

//    public void setParent(Tetromino parent){
//        this.parent = parent;
//    }
//
//    public Tetromino getParent(){
//        return parent;
//    }
}
