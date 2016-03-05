import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.List;
import java.util.Random;

public class ServerSenderThread implements Runnable {

    //private ObjectOutputStream[] outputStreamList = null;
    private List<Socket> socketList = null;
    private List<ObjectOutputStream> outList = null;
    private Socket socket = null;
    
    public ServerSenderThread(List<Socket> socketList, List<ObjectOutputStream> outList, Socket socket){
        this.socketList = socketList;
        this.outList = outList;
        this.socket = socket;
    }

    /*
     *Handle the initial joining of players including 
      position initialization
     */
    public void handleHello(){
        
        //The number of players
        int playerCount = socketList.size();
        Random randomGen = null;
        Player[] players = new Player[playerCount]; //These are dynamically allocated to be the right size.
        
        if(Debug.debug) System.out.println("In handleHello");
        MPacket hello = null;
        try{       
        	
            for(int i=0; i<playerCount; i++){

                if(randomGen == null){
                   randomGen = new Random(hello.mazeSeed); 
                }
                //Get a random location for player
                Point point =
                    new Point(randomGen.nextInt(hello.mazeWidth),
                          randomGen.nextInt(hello.mazeHeight));
                
                String ip = socket.getRemoteSocketAddress().toString();
                int port = socket.getPort();
                
                //Start them all facing North
                Player player = new Player(hello.name, point, Player.North, ip, port);
                players[i] = player;
            }
            
            hello.event = MPacket.HELLO_RESP;
            hello.players = players;
            //Now broadcast the HELLO
            if(Debug.debug) System.out.println("Sending " + hello);
            
            //TODO MUTEX THIS BITCH
            outList.add(new ObjectOutputStream(socket.getOutputStream()));
            
            for(ObjectOutputStream out: outList){
            	//Once it is made use it to send.
                out.writeObject(hello);
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
