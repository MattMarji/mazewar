import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class ClientEventListenerThread implements Runnable {

	
	private MSocket mSocket  =  null;
    private Hashtable<String, Client> clientTable = null;
    private Maze maze;
    private List<Player> players = null;
    private List<Boolean> ackList = null;
    private BlockingQueue eventQueue = null;
    private String name = null;
    private AtomicBoolean hasToken;
    private double retransTimer = 0;
    
    
    // This thread will be spawned once per client and will listen to a particular client for events
    public ClientEventListenerThread(MSocket mSocket, List<Player> players, List<Boolean> ackList,
            Hashtable<String, Client> clientTable, Maze maze, BlockingQueue eventQueue, String name, AtomicBoolean hasToken, double retransmissionTimer){
		this.mSocket = mSocket;
		this.clientTable = clientTable;
		this.maze = maze;
		this.players = players;
		this.ackList = ackList;
		this.eventQueue = eventQueue;
		this.name = name;
		this.hasToken = hasToken;
		this.retransTimer = retransmissionTimer;
		
		if(Debug.debug) System.out.println("Instatiating ClientEventListenerThread");
	}

    public void run() {
        MPacket received = null;
        Client client = null;
        if(Debug.debug) System.out.println("Starting ClientEventListenerThread");
        
        while(true){
            try{
                received = (MPacket) mSocket.readObjectNoError();
                if(Debug.debug) System.out.println("ClientEventListener: Received " + received + "Received Type: " + received.event);
                client = clientTable.get(received.name);
                
                // If it is a TOKEN packet, accept token and execute next event!
                if (received.event == MPacket.TOKEN_SEND) {
                	hasToken.set(true);
                	new Thread(new ClientSenderThread(eventQueue, clientTable, players, name, hasToken)).start();
                	
                	//Start the ACK thread.
                	new Thread(new ClientAckThread(ackList, eventQueue, hasToken, clientTable, name, players)).start();                	
                	
                	
                	//Start retransmission timer and wait for ACK. TODO: Check to see if Java Thread Timer can interrupt.
                	Timer retransTimeout = new Timer();
                	
                	TimerTask retrans = new TimerTask() {
						
						@Override
						public void run() {
							//The mSocket we have here should be what is connecting me to the client who I haven't gotten an Ack from. 
							MPacket retransPkt = (MPacket) eventQueue.peek();
							mSocket.writeObjectNoError(retransPkt);
							
						}
					};
                	
                	retransTimeout.schedule(retrans, (long) retransTimer);
                	//Wait for ACK from this client based on the event that I just sent.
                	
                	//This is blocking, but should be interrupted by the Timer! 
                	received = (MPacket) mSocket.readObjectNoError();
                	
                	if(received.event == MPacket.ACK){
                		//This means we're good, we got the ACK, we just need to cancel the scheduled timer interrupt
                		retransTimeout.cancel();
                		retransTimeout.purge();
                		//TODO: THIS NEXT BIT IS WRONG. We need to set the flag in the ackList to true for this guy.
                		players.indexOf(clientTable.get(received.name));
                	}
                	
                	//TODO Set ACK flag in ackList to true
                	
                } 
                
                // If it is another CLIENT introducing themselves, we accept!
                else if (received.event == MPacket.HELLO_INIT) {
                	
                	// A client has established a connection with us. Save the mSocket they connected with us on.
                	// Now we can talk to the player via the mSocket.
                	for (Player player: players) {
                		if (received.name.equals(player.name)) {
                			player.mSocket = mSocket;
                			//If I have the token, we're assuming that I've sent out an action and could be waiting for acks back from others, so don't add people yo my ack list.
                			if(hasToken.get())
                				System.out.println("Has token and waiting in loop.");
                			while(hasToken.get()) {
                				;
                			}
                			System.out.println("Surrendered token");
                			ackList.add(false);
                		}
                	}
                } else {
                	try {
                		// We received the event. We now want to send an ACK.
                    	executeEvent(client, received.event);
                    	
                    } catch (UnsupportedOperationException e) {
                    	System.out.println("We received some weird operation that caused us to throw an UnsopportedOperationException!");
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
