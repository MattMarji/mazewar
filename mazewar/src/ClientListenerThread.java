import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class ClientListenerThread implements Runnable {
	
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
    private Hashtable<String, Client> clientTable = null;
    private Maze maze;
    private List<Player> players = null;
    private BlockingQueue eventQueue = null;
    private String clientName;
    private AtomicBoolean latestClient = new AtomicBoolean(true);
    public AtomicBoolean hasToken;
   
    
    public ClientListenerThread(ObjectInputStream in, ObjectOutputStream out,
            Hashtable<String, Client> clientTable, Maze maze, List<Player> players, BlockingQueue eventQueue, String clientName, AtomicBoolean hasToken){
		this.in = in;
		this.clientTable = clientTable;
		this.maze = maze;
		this.players = players;
		this.eventQueue = eventQueue;
		this.out = out;
		this.clientName = clientName;
		this.hasToken = hasToken;
		
		if(Debug.debug) System.out.println("Instatiating ClientListenerThread");
	}

    public void run() {
        MPacket received = null;
        Client client = null;
        if(Debug.debug) System.out.println("Starting ClientListenerThread");
        while(true){
            try{
                received = (MPacket) in.readObject();
                client = clientTable.get(received.name);
                
                // If it is a missile tick we service it right away.
                if (received.event == MPacket.MISSILE_TICK) {
                	//System.out.println("Received " + received + "Received Type: " + received.event);
                	maze.syncBullets();
                }
                 
                // If it is a HELLO_RESP from the server, update our Player list and connect to new players
                if (received.event == MPacket.HELLO_RESP) {
                	System.out.println("ClientListener: Received " + received + "Received Type: " + received.event);
                	if(this.latestClient.get()) {
                		connectToPeers(received.players);
                	}
                	this.latestClient.set(false);
                }
                
                // If it is a TOKEN packet, accept token and execute next event!
                if (received.event == MPacket.TOKEN_SEND) {
                	hasToken.set(true);
                	System.out.println("Received " + received + "Received Type: " + received.event);
                	
                     new Thread(new ClientSenderThread(eventQueue, clientTable, players, clientName, hasToken)).start();
                     
                   //Start the ACK thread.
                 	new Thread(new ClientAckThread(null, eventQueue, hasToken, clientTable, clientName, players)).start(); 
                 	
                	/*Pop off the eventQueue, execute event, pass on the token. So we need to have access to the clientList
                	 * Don't actually want to do the work here I don't think.
                	 * eventQueue.take(); -- can be done on listener/sender 
                	 * broadcast event to all clients -- can be done on sender
                 	 * Then we need to wait for ACKs to come back -- needs to be done on listener
                	 * executeEvent(me,event.event); -- can be done on listener/sender
                	 * token handoff to the next guy
                	 */
                	
                } 
                   
            }catch(IOException e){
                Thread.currentThread().interrupt();    
            } catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
    }
    
    // TODO Rethink logic involved with this -- population of clients should only occur when the client connects to us.
    // Cannot assume we show them on GUI right away.
    private void connectToPeers(List<Player> newPlayers) {
    	// We must update our client list now. Compare our list to the one received.
    	boolean hasMatch = false;
    	
    	if (newPlayers.size() <= 1)
    		return;
    	
    	int idx;
    	Point spawnPoint;
    	
    	for(Player player : newPlayers) {
    		
    		idx = newPlayers.indexOf(player);
    		spawnPoint = new Point(idx,idx);
    		
    		// Compare names to determine if we already have them.
    		for (Player currPlayer : this.players) {
    			if (currPlayer.name.equals(player.name)) {
    				System.out.println("We already have a connection to Player: " + player.name);
    				hasMatch = true;
    				break;
    			}
    		}
    		
    		// Only create a socket to the player if we do not already have one.
    		if (!hasMatch) {
    			try {
        			// Connect to new client 
					player.mSocket = new MSocket(player.ip, player.port);
					
					// Add player to our Client-level playerList
					this.players.add(player);
					
					// Add client to our maze!
					RemoteClient remoteClient = new RemoteClient(player.name);
                    //maze.addClientAt(remoteClient, player.point, player.direction);
					maze.addClientAt(remoteClient, spawnPoint, player.direction);
                    clientTable.put(player.name, remoteClient);
					
                    System.out.println("Added Player " + player.name + " to playerList.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		
    		// Reset flag.
    		hasMatch = false;
    		
    	}
    }

}
