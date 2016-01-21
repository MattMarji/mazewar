import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

public class ClientSenderThread implements Runnable {

    private MSocket mSocket = null;
    private BlockingQueue<MPacket> eventQueue = null;
    private Integer seqNum = 0;
    
    public ClientSenderThread(MSocket mSocket,
                              BlockingQueue eventQueue){
        this.mSocket = mSocket;
        this.eventQueue = eventQueue;
        this.seqNum = 1;
    }
    
    public void run() {
        MPacket toServer = null;
        if(Debug.debug) System.out.println("Starting ClientSenderThread");
        while(true){
            try{                
                //Take packet from queue
                toServer = (MPacket)eventQueue.take();
                //if(toServer.sequenceNumber == 0) { Probably don't even need this check, seeing as all packets sent from the client to the server have sequence number 0.
                	toServer.sequenceNumber = seqNum;
                	seqNum ++;
            	//}
                	
                if(Debug.debug) System.out.println("Sending " + toServer);
                mSocket.writeObject(toServer);    
            }catch(InterruptedException e){
                e.printStackTrace();
                Thread.currentThread().interrupt();    
            }
            
        }
    }
}
