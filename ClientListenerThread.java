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
    private Integer lowSeqNum = 0;
    private	MPacket[] pktArr = null; 
    
    
    public ClientListenerThread( MSocket mSocket,
                                Hashtable<String, Client> clientTable){
        this.mSocket = mSocket;
        this.clientTable = clientTable;
        this.globalSeqNum = 1;	/*We know this to be the initial value that the server sends. We also know all clients must join before the game can begin, so they will all inevitably be waiting for pkt with seq# 1 first*/
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
                System.out.println("Received " + received);
                client = clientTable.get(received.name);
                
                if(received.sequenceNumber == globalSeqNum) {/*if this is the next packet we are expecting all is well, 
                execute its event*/
        
                	if(Debug.debug) System.out.println("recvd correct pkt #: " + received.sequenceNumber);
                	
                	executeEvent(client, received.event);
                    globalSeqNum ++;	/*Increment the Global Sequence number, i.e. the next pkt we're expecting. 
                    ONLY DO THIS IF we've got the previous seq# we were looking for and and executed it. 
                    Only do this after executeEvent.
                    Could potentially inline it in there if we wanted to, but that makes code slightly less readable.*/
                	
                } else { /*The packet we received is out of order, and so we need to store it somewhere so we can use it
                	when it is time for it to be used.*/
                	System.out.println("DEBUG: RECV'D PKT #: " + received.sequenceNumber + " GLOBAL PACKET #: " + globalSeqNum);
                	if(received.sequenceNumber < lowSeqNum) 
                		lowSeqNum = received.sequenceNumber;
                	
                	pktArr[(received.sequenceNumber)%ARRAY_SIZE] = received;
                	      	
                	
                }
                                
                if(globalSeqNum >= lowSeqNum) { /*This means that the next action we're expecting could have already been 
                received and be currently stored in the pktArr. Check the pktArr for it at the relevant index.*/
                	
                	if(pktArr[globalSeqNum%ARRAY_SIZE] != null) {
                		MPacket candidate = pktArr[globalSeqNum%ARRAY_SIZE];
                		
                		if(candidate.sequenceNumber == globalSeqNum) {//We found what we're looking for! Execute it
                			pktArr[globalSeqNum%ARRAY_SIZE] = null;//Clear that index in the array! 
                			
                			if(Debug.debug) System.out.println("correct pkt was stored #: " + candidate.sequenceNumber);
                			
                			executeEvent(client, candidate.event);
                            globalSeqNum ++; /*Increment the Global Sequence number, i.e. the next pkt we're expecting.
                            ONLY DO THIS IF we've got the previous seq# we were looking for and and executed it. 
                            Only do this after executeEvent.
                            Could potentially inline it in there if we wanted to, but that makes code slightly less readable.*/
                			
                		} else { /*This is the dreaded case! We've gotten the corner case we discussed of 2 & 258 or whatever 
                		else we need to figure out some elegant way to handle this highly unlikely edge case*/
                			
                			client.unregisterMaze(); //CLIENT.ABORTLIFE shit be fucked homie, it's time to peace the fuck out.
                			
                		}
                	} else {/*This means that there was nothing stored at that index in the array, so we haven't received the
                	packet we're looking for (in the sequence) This means we need to keep waiting to receive it from the
                	server. This is only here for clarity/readability's sake, there is no active code here.
                	REMOVE*/
                		
                	}
                	
                } else {/*The next packet we want in terms of sequence numbers has definitely not been received and stored 
                yet, this means that there is no need to check the array for it.
                This is only here for clarity/readability's sake, there is no active code here.
                REMOVE*/
                	
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
