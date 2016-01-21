import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;

public class ServerListenerThread implements Runnable {

    private MSocket mSocket =  null;
    private BlockingQueue eventQueue = null;
    private Integer/*[]*/ clientSeqNum = 0;
    private HashMap<Integer,MPacket> recvdPkts = null;
    private Boolean checkStoredPkt = true;
    private MPacket storedPkt = null;
    /*There is a listener thread per client here, so I think we realistically only need to look at the one variable, and don't need to store them on a per client basis.
    If it turns out that we do need to keep them on a per client basis, then uncomment the array stuff both here and below*/

    public ServerListenerThread( MSocket mSocket, BlockingQueue eventQueue){
        this.mSocket = mSocket;
        this.eventQueue = eventQueue;
		this.clientSeqNum = 0;//new Integer[Server.MAX_CLIENTS];
		this.recvdPkts = new HashMap<Integer, MPacket>();
		this.checkStoredPkt = false;
		this.storedPkt = null;
    }

    public void run() {
        MPacket received = null;
        if(Debug.debug) System.out.println("Starting a listener");
        while(true){
            try{
                received = (MPacket) mSocket.readObject();
                if(Debug.debug) System.out.println("Received: " + received);
		
		if(received.sequenceNumber == clientSeqNum) {
                	eventQueue.put(received);
			clientSeqNum ++;
			
		} else {
			recvdPkts.put(received.sequenceNumber, received);
		}
		
		
		checkStoredPkt = true;
		
		while(checkStoredPkt) {
			storedPkt = recvdPkts.get(clientSeqNum);
			
			if(storedPkt != null) {
				recvdPkts.remove(clientSeqNum);
				eventQueue.put(storedPkt);
				clientSeqNum ++;
			} else 
				checkStoredPkt = false;
		
		}
		
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();    
            }catch(IOException e){
                Thread.currentThread().interrupt();
            }catch(ClassNotFoundException e){
                Thread.currentThread().interrupt();    
            }
            
        }
    }
}