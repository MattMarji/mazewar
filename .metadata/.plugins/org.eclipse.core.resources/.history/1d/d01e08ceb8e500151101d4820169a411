import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerSenderThread implements Runnable {

    //private ObjectOutputStream[] outputStreamList = null;
    private List<Socket> socketList = null;
    private List<ObjectOutputStream> outList = null;
    private Socket socket = null;
    private MPacket helloPacket = null;
    private Boolean oneTime = null;
    
    public ServerSenderThread(List<Socket> socketList, List<ObjectOutputStream> outList, Socket socket, MPacket helloPacket, Boolean oneTime){
        this.socketList = socketList;
        this.outList = outList;
        this.socket = socket;
        this.helloPacket = helloPacket;
        this.oneTime = oneTime;
    }

    /*
     *Handle the initial joining of players including 
      position initialization
     */
    public void handleHello(){
        
        //The number of players
        int playerCount = socketList.size();
        Random randomGen = null;
        List<Player> players = new ArrayList<Player>();
 
        
        if(Debug.debug) System.out.println("In handleHello");
        try{       
        	
            for(int i=0; i<playerCount; i++){

                if(randomGen == null){
                   randomGen = new Random(this.helloPacket.mazeSeed); 
                }
                //Get a random location for player
                Point point =
                    new Point(randomGen.nextInt(this.helloPacket.mazeWidth),
                          randomGen.nextInt(this.helloPacket.mazeHeight));
                
                InetSocketAddress sockaddr = (InetSocketAddress) socket.getRemoteSocketAddress();
                String ip = sockaddr.getAddress().getHostAddress();
                
                int port = socket.getPort();
                
                //Start them all facing North
                Player player = new Player(this.helloPacket.name, point, Player.North, ip, port);
                players.add(player);
            }
            MPacket helloResponse = new MPacket("CNS", MPacket.HELLO, MPacket.HELLO_RESP);
            helloResponse.players = players;
            //Now broadcast the HELLO
            if(Debug.debug) System.out.println("Sending " + helloResponse);
            
            //TODO MUTEX THIS BITCH
            outList.add(new ObjectOutputStream(socket.getOutputStream()));
            
            for(ObjectOutputStream out: outList){
            	//Once it is made use it to send.
                out.writeObject(helloResponse);
            }
            
            if(oneTime) {
            	ObjectOutputStream tokenHolder = outList.get(0);
                MPacket tokenSend = new MPacket("CNS", MPacket.TOKEN, MPacket.TOKEN_SEND);
                tokenHolder.writeObject(tokenSend);
                oneTime = false;
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
