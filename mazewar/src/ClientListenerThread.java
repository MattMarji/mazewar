import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* WELCOME TO THE MOST OVER-COMMENTED CODE IN THE WORLD! hahaha enjoy! Lemme know if there is anything you don't agree with!
 * The code could definitely stand to be refactored a little, but lets check validity first.
 * -!- KF 17:37 18/1/2016*/

public class ClientListenerThread implements Runnable {
	
	private ObjectInputStream in = null;
    private Hashtable<String, Client> clientTable = null;
    private Integer globalSeqNum = 0;
    private Maze maze;
    private List<Player> players = null;
    
    
    public ClientListenerThread(ObjectInputStream in,
                                Hashtable<String, Client> clientTable, Maze maze){
        this.in = in;
        this.clientTable = clientTable;
        this.maze = maze;
        
        if(Debug.debug) System.out.println("Instatiating ClientListenerThread");
    }
    
    public ClientListenerThread(ObjectInputStream in,
            Hashtable<String, Client> clientTable, Maze maze, List<Player> players){
		this.in = in;
		this.clientTable = clientTable;
		this.maze = maze;
		this.players = players;
		
		if(Debug.debug) System.out.println("Instatiating ClientListenerThread");
	}

    public void run() {
        MPacket received = null;
        Client client = null;
        if(Debug.debug) System.out.println("Starting ClientListenerThread");
        while(true){
            try{
                received = (MPacket) in.readObject();
                System.out.println("Received " + received + "Received Type: " + received.event);
                client = clientTable.get(received.name);
                
                // If it is a missile tick we service it right away.
                if (received.event == MPacket.MISSILE_TICK) {
                	maze.syncBullets();
                }
                //TODO: We also need to have a check here for HELLO_RESPONSEs because those should hold updated client lists.
                
                // If it is a TOKEN packet, accept token and execute next event!
                if (received.event == MPacket.TOKEN_SEND) {
                	/*Pop off the eventQueue, execute event, pass on the token. So we need to have access to the clientList
                	 * Don't actually want to do the work here I don't think.
                	 * ClientSenderThread should broadcast the packet to all clients, maybe we can trigger this using a flag?
                	 * Then we need to wait for ACKs to come back
                	 * eventQueue.take();
                	 * broadcast event to all clients
                	 * wait for ACKS to come back from everyone
                	 * executeEvent(me,event.event);
                	 * token handoff to the next guy
                	 */
                } 
                
                //TODO: MAKE SURE TO DEAL WITH HELLO_RESPONSE PACKETS ACCEPT NEW MSOCKET.
                
                
                // CASE 1: Received Seq # EQUAL Global Seq #
                /*if (received.sequenceNumber == globalSeqNum) {
                	System.out.println("DEBUG: MATCHING SEQ NUMBERS!");
                	globalSeqNum++;
                	executeEvent(client, received.event);
                }
                
                else {
                	// CASE 2: Not a match, store in buffer...
                    System.out.println("DEBUG: NOT A MATCH!");
                    System.out.println("DEBUG: REC SEQ #: " + received.sequenceNumber  + " GLOBAL SEQ #: " + globalSeqNum);
                    
                    pktArr[(received.sequenceNumber)%ARRAY_SIZE] = received;
                }

                // if we find the next sequence number in the queue, lets process it.
                while (pktArr[(globalSeqNum)%ARRAY_SIZE] != null) {
                	System.out.println("DEBUG: FOUND GLOBAL SEQ NUM: " + globalSeqNum);
                	MPacket pkt = pktArr[(globalSeqNum)%ARRAY_SIZE];
                	client = clientTable.get(pkt.name);
                	executeEvent(client, pkt.event);
                	pktArr[(globalSeqNum)%ARRAY_SIZE] = null;
                	globalSeqNum++;
                }*/

                   
            }catch(IOException e){
                Thread.currentThread().interrupt();    
            } catch (ClassNotFoundException e) {
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
