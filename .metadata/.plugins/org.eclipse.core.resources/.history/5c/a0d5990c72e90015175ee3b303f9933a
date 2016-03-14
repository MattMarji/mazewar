import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Player implements Serializable {
    //Need these because direction is not serializable
    public final static int North = 0;
    public final static int South = 1;
    public final static int East  = 2;
    public final static int West  = 3;
    
    public Point point = null;
    public int direction;
    public String name;
    public String ip;
    public int port;
    public ObjectOutputStream out;
    public ObjectInputStream in;
    public Socket socket;
    public MSocket mSocket;

    public Player(String name, Point point, int direction){
        this.point = point;
        this.name = name;
        this.direction = direction;
    }
    
    public Player(String name, Point point, int direction, String ip, int port){
        this.point = point;
        this.name = name;
        this.direction = direction;
        this.ip = ip;
        this.port = port;
    }
    
    public Player(String name, Point point, int direction, String ip, int port, Socket socket, ObjectOutputStream out, ObjectInputStream in){
        this.point = point;
        this.name = name;
        this.direction = direction;
        this.ip = ip;
        this.port = port;
        this.socket = socket;
        this.out = out;
        this.in = in;
    }
    
    public String toString(){
    	return "[" + name + ": (" + point.getX() + "," + point.getY() + ")]"; 
    }

}