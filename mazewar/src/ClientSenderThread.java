import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ClientSenderThread implements Runnable {

    private MSocket mSocket = null;
    private ObjectOutputStream out = null;
    private BlockingQueue<MPacket> eventQueue = null;
    private Hashtable<String, Client> clientTable = null;
    private List<Player> players = null;
    
    public ClientSenderThread(ObjectOutputStream out,
                              BlockingQueue eventQueue,
                              Hashtable<String, Client> clientTable,
                              List<Player> players){
        this.out = out;
        this.eventQueue = eventQueue;
        this.clientTable = clientTable;
        this.players = players;
    }
    
    public void run() {
        if(Debug.debug) System.out.println("Starting ClientSenderThread");
    	
        Thread.currentThread();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        if (eventQueue.size() >= 1) {
    		try {
				MPacket next = eventQueue.take();
				
				// TODO We will now send this packet to all players via broadcast.
				
				// TODO We block and wait for all acks via sudo-TCP and then we executeEvent!
				Client client = clientTable.get(next.name);
				executeEvent(client, next.event);
				
				// We send token to the next client
				for (Player player: players) {
					// Get this client. Determine next person in line.
					if (player.name.equals(next.name)) {
						int index = players.indexOf(player);
						int size = players.size();
						MPacket tokenSend = new MPacket(player.name, MPacket.TOKEN, MPacket.TOKEN_SEND);
						
						Player tokenPlayer = null; 
						
						if (index == (size-1)) {
							// Pass token to first person.
							tokenPlayer = players.get(0);
						} else {
							tokenPlayer = players.get(index+1); 
						}
						
						// Send token to tokenPlayer.
						// ASSUME: We are keeping the mSocket of each player updated once we connect!
						tokenPlayer.mSocket.writeObject(tokenSend);
						
						break;
					}
				}
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
