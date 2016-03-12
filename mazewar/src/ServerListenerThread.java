import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerListenerThread implements Runnable {

    private Socket socket =  null;
    private BlockingQueue eventQueue = null;
    private Integer/*[]*/ clientSeqNum = 0;
    private HashMap<Integer,MPacket> recvdPkts = null;
    private ObjectInputStream in = null;
    private List<ObjectOutputStream> out = null;
    private int clientCount; //The number of clients before game starts
    private List<Socket> socketList = null; //A list of MSockets
    private List<String> clientList = null;
    private List<Player> playerList = null;
    private Boolean helloRecvd = false;
    private AtomicBoolean oneTime;
    
    /*There is a listener thread per client here, so I think we realistically only need to look at the one variable, and don't need to store them on a per client basis.
    If it turns out that we do need to keep them on a per client basis, then uncomment the array stuff both here and below*/

    public ServerListenerThread(Socket socket, List<ObjectOutputStream> out, int clientCount, List<Socket> socketList, List<String> clientList, List<Player> playerList, AtomicBoolean oneTime){
        this.socket = socket;
		this.clientSeqNum = 0;
		this.recvdPkts = new HashMap<Integer, MPacket>();
		this.out = out;
        this.socketList = socketList;
        this.clientList = clientList;
        this.playerList = playerList;
        this.clientCount = clientCount;   
        this.oneTime = oneTime;
    }

    public void run() {
        MPacket received = null;
        if(Debug.debug) System.out.println("Starting a listener");
        
        try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        while(!helloRecvd){
            try{                     
                 				
 				//if(in.available() > 0) {
 					//so we've now pulled the incoming data from the socket and stream, we need to store it in the ingress queue.
 					received = (MPacket) in.readObject();
 					
 					if( !(received.type == MPacket.HELLO) || !(received.event == MPacket.HELLO_INIT)) {
 	 					return; 
 	 				}			
                 
                 if(Debug.debug) System.out.println("Received: " + received);
                 
                 String incomingName = received.name.toLowerCase();
                 
                 if(clientList == null) {
                	 try {
						throw new Exception();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                 }
                 
                 //TODO check that the recvd pkt is a hello. 
                 if(clientList.isEmpty() || clientList.size() == 0 || !(clientList.contains(incomingName))) {
                	 clientList.add(incomingName);
                     //clientCount++;
                     socketList.add(socket);
                     System.out.println("Client: " + incomingName + " added to the game, there are now " + clientCount + " clients.");
                     helloRecvd = true;
                     
                     //Start a new sender thread here to broadcast our new clientList to everyone who is playing.
                     new Thread(new ServerSenderThread(socketList, playerList, out, socket, received, oneTime, received.ip, received.port)).start();
                     
                 } else {
                 
                 	//Check that there isn't already a client with this name in our game, because all clients need to have unique names.

                 	System.out.println("A client with the name: " + incomingName + " already exists. Please select another name.");
                 	return;
                 }
                 
 				//}  
	
            }catch(IOException e){
                Thread.currentThread().interrupt();
            }catch(ClassNotFoundException e){
                Thread.currentThread().interrupt();    
            }
            
        }
        return;
    }
    
}