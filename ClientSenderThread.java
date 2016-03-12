import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientSenderThread implements Runnable {

    private MSocket mSocket = null;
    private ObjectOutputStream out = null;
    private BlockingQueue<MPacket> eventQueue = null;
    private Hashtable<String, Client> clientTable = null;
    private List<Player> players = null;
    private String clientName = null;
    private AtomicBoolean hasToken;
    
    public ClientSenderThread(BlockingQueue eventQueue,
                              Hashtable<String, Client> clientTable,
                              List<Player> players,
                              String clientName, AtomicBoolean hasToken){
        this.eventQueue = eventQueue;
        this.clientTable = clientTable;
        this.players = players;
        this.clientName = clientName;
        this.hasToken = hasToken;
    }
    
    public void run() {
        if(Debug.debug) System.out.println("Starting ClientSenderThread. The value of hasToken is: " + hasToken);
        
        while (players.size() <= 1) {
        	if (eventQueue.size() >= 1) {
        		try {
        			// Event Queue only stores events for this client.
    				MPacket next = eventQueue.take();
    				
    				// TODO We block and wait for all acks via sudo-TCP and then we executeEvent!
    				Client client = clientTable.get(clientName);
    				executeEvent(client, next.event);
    				
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	}
        }

        if (eventQueue.size() >= 1) {
        	try {
    			
    			// Event Queue only stores events for this client.
				MPacket next = eventQueue.take();
				
				// TODO We will now send this packet to all players via broadcast.
				for (Player player: players) {
					// Get this client. Determine next person in line.
					if (!player.name.equals(clientName)) {
						//TODO Use writeObject with error!
						player.mSocket.writeObjectNoError(next);
					}
				}
				
				// TODO We block and wait for all acks via sudo-TCP and then we executeEvent!
				Client client = clientTable.get(clientName);
				executeEvent(client, next.event);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        
        // We send token to the next client
		for (Player player: players) {
			// Get this client. Determine next person in line.
			if (player.name.equals(clientName)) {
				int index = players.indexOf(player);
				int size = players.size();
				MPacket tokenSend = new MPacket(player.name, MPacket.TOKEN, MPacket.TOKEN_SEND);
				
				Player tokenPlayer = null; 
				

				tokenPlayer = players.get((index + 1) % size);
								
				// Send token to tokenPlayer.
				// ASSUME: We are keeping the mSocket of each player updated once we connect!
				//tokenPlayer.mSocket.writeObject(tokenSend);
				
				//TODO Use writeObject with error!
				tokenPlayer.mSocket.writeObjectNoError(tokenSend);
				hasToken.set(false);
				break;
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
