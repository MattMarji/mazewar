import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ClientEventListenerThread implements Runnable {

	
	private MSocket mSocket  =  null;
    private Hashtable<String, Client> clientTable = null;
    private Maze maze;
    private List<Player> players = null;
    private BlockingQueue eventQueue = null;
    private String name = null;
    
    
    // This thread will be spawned once per client and will listen to a particular client for events
    public ClientEventListenerThread(MSocket mSocket, List<Player> players,
            Hashtable<String, Client> clientTable, Maze maze, BlockingQueue eventQueue, String name){
		this.mSocket = mSocket;
		this.clientTable = clientTable;
		this.maze = maze;
		this.players = players;
		this.eventQueue = eventQueue;
		this.name = name;
		
		if(Debug.debug) System.out.println("Instatiating ClientEventListenerThread");
	}

    public void run() {
        MPacket received = null;
        Client client = null;
        if(Debug.debug) System.out.println("Starting ClientEventListenerThread");
        
        while(true){
            try{
                received = (MPacket) mSocket.readObjectNoError();
                System.out.println("ClientEventListener: Received " + received + "Received Type: " + received.event);
                client = clientTable.get(received.name);
                
                // If it is a TOKEN packet, accept token and execute next event!
                if (received.event == MPacket.TOKEN_SEND) {
                	new Thread(new ClientSenderThread(eventQueue, clientTable, players, name)).start();
                } 
                
                // If it is another CLIENT introducing themselves, we accept!
                if (received.event == MPacket.HELLO_INIT) {
                	
                	// A client has established a connection with us. Save the mSocket they connected with us on.
                	// Now we can talk to the player via the mSocket.
                	for (Player player: players) {
                		if (received.name.equals(player.name)) {
                			player.mSocket = mSocket;
                		}
                	}
                }

            }catch(IOException e){
                Thread.currentThread().interrupt();    
            }           
        }
    }
    
    private void executeEvent (Client client, int event) throws UnsupportedOperationException {
    	
    	if(event == MPacket.UP){
            client.forward();
            if(Debug.debug) System.out.println("client: " +client.getName() + " executeEvent UP");
        }else if(event == MPacket.DOWN){
            client.backup();
            if(Debug.debug) System.out.println("client: " +client.getName() + " executeEvent DOWN");
        }else if(event == MPacket.LEFT){
            client.turnLeft();
            if(Debug.debug) System.out.println("client: " +client.getName() + " executeEvent LEFT");
        }else if(event == MPacket.RIGHT){
            client.turnRight();
            if(Debug.debug) System.out.println("client: " +client.getName() + " executeEvent RIGHT");
        }else if(event == MPacket.FIRE){
            client.fire();
            if(Debug.debug) System.out.println("client: " +client.getName() + " executeEvent FIRE");
        }else{
            throw new UnsupportedOperationException();
        }
    	
    }

}
