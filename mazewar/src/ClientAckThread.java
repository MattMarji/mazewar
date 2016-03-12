import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientAckThread implements Runnable{
	List<Boolean> ackList = null;
	AtomicBoolean hasToken = null;
	private List<Player> players = null;
	BlockingQueue eventQueue = null;
    private Hashtable<String, Client> clientTable = null;
    String clientName = null;
	
    public ClientAckThread(List<Boolean> ackList, BlockingQueue eventQueue, AtomicBoolean hasToken, Hashtable<String, Client> clientTable, String clientName, List<Player> players){
    	this.ackList = ackList;
    	this.hasToken = hasToken;
    	this.players = players;
    	this.eventQueue = eventQueue;
    	this.clientTable = clientTable;
    	this.clientName = clientName; 
    }
    
    public void run() {
    	if(hasToken.get()) {
    		
    	}
    	
    	//Tight loop here until the ackList contains all true values, meaning we've got all our ACKs
    	while(ackList.contains(false)) {
    		try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	//If we've left, the loop, then we've got all the ACKs for this action, so we should pull it from the head of the queue, do it, and then pass off the token
    	MPacket nextEvent = null;
		try {
			nextEvent = (MPacket) eventQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	Client client = clientTable.get(clientName);
    	executeEvent(client, nextEvent.event);
    	
    	handOffToken(players);

    }
    
    private void handOffToken (List<Player> players) {
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