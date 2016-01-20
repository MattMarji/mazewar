import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* WELCOME TO THE MOST OVER-COMMENTED CODE IN THE WORLD! hahaha enjoy! Lemme know if there is anything you don't agree with!
 * The code could definitely stand to be refactored a little, but lets check validity first.
 * -!- KF 17:37 18/1/2016*/

public class ClientListenerThread implements Runnable {

	public static final int ARRAY_SIZE = 1024; //ATTENTION! I ADDED THIS SO IT IS EASILY CONFIGURABLE AND CODE IS MORE READABLE
	
	private MSocket mSocket  =  null;
    private Hashtable<String, Client> clientTable = null;
    private Integer globalSeqNum = 0;
    private	MPacket[] pktArr = null; 
    
    
    public ClientListenerThread( MSocket mSocket,
                                Hashtable<String, Client> clientTable){
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        this.pktArr = new MPacket[ARRAY_SIZE];
        if(Debug.debug) System.out.println("Instatiating ClientListenerThread");
    }

    public void run() {
        MPacket received = null;
        Client client = null;
        if(Debug.debug) System.out.println("Starting ClientListenerThread");
        while(true){
            try{
                received = (MPacket) mSocket.readObject();
                System.out.println("Received " + received + "Received Type: " + received.event);
                client = clientTable.get(received.name);
                
                // CASE 1: Received Seq # EQUAL Global Seq #
                if (received.sequenceNumber == globalSeqNum) {
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
                }

                   
            }catch(IOException e){
                Thread.currentThread().interrupt();    
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }            
        }
    }
    
    private void executeEvent (Client client, int event) throws UnsupportedOperationException {
    	
    	if(Debug.debug) System.out.println("Client: " +client.getName() + " Starting executeEvent");
    	
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
    
//	if(received.event == MPacket.UP){
//  client.forward();
//}else if(received.event == MPacket.DOWN){
//  client.backup();
//}else if(received.event == MPacket.LEFT){
//  client.turnLeft();
//}else if(received.event == MPacket.RIGHT){
//  client.turnRight();
//}else if(received.event == MPacket.FIRE){
//  client.fire();
//}else{
//  throw new UnsupportedOperationException();
//} 
    
}
