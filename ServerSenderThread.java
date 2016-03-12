import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerSenderThread implements Runnable {

    private List<Socket> socketList = null;
    private List<Player> playerList = null;
    private List<ObjectOutputStream> outList = null;
    private Socket socket = null;
    private MPacket helloPacket = null;
    private AtomicBoolean oneTime;
    private String ip = null;
    private int port = 0;
    
    public ServerSenderThread(List<Socket> socketList, List<Player> playerList, List<ObjectOutputStream> outList, Socket socket, MPacket helloPacket, AtomicBoolean oneTime, String ip, int port){
        this.socketList = socketList;
        this.playerList = playerList;
        this.outList = outList;
        this.socket = socket;
        this.helloPacket = helloPacket;
        this.oneTime = oneTime;
        this.ip = ip;
        this.port = port;
    }

    /*
     *Handle the initial joining of players including 
      position initialization
     */
    public void handleHello(){
        
        //The number of players
        int playerCount = socketList.size();
        Random randomGen = null;
 
        
        if(Debug.debug) System.out.println("In handleHello");
        try{       
        	
                if(randomGen == null){
                   randomGen = new Random(this.helloPacket.mazeSeed); 
                }
                //Get a random location for player
                Point point =
                    new Point(randomGen.nextInt(this.helloPacket.mazeWidth),
                          randomGen.nextInt(this.helloPacket.mazeHeight));
                
                //Start them all facing North
                playerList.add(new Player(this.helloPacket.name, point, Player.North, this.ip, this.port));
                
            MPacket helloResponse = new MPacket("CNS", MPacket.HELLO, MPacket.HELLO_RESP);
            helloResponse.players = playerList;
            //Now broadcast the HELLO
            if(Debug.debug) System.out.println("Sending " + helloResponse);
            
            //TODO MUTEX THIS BITCH
            outList.add(new ObjectOutputStream(socket.getOutputStream()));
            
            for(ObjectOutputStream out: outList){
            	//Once it is made use it to send.
            	out.reset();
                out.writeObject(helloResponse);
            }
            
            if(oneTime.get()) {
            	ObjectOutputStream tokenHolder = outList.get(0);
                MPacket tokenSend = new MPacket("CNS", MPacket.TOKEN, MPacket.TOKEN_SEND);
                tokenHolder.writeObject(tokenSend);
                oneTime.set(false);
            }
            
            
        }catch(IOException e){
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
    
    public void run() {
        
		handleHello();
		//Mutex this badboy	
		
		return;
    }
}
