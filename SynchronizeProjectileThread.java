import java.lang.Thread;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SynchronizeProjectileThread implements Runnable  {
    
	private List<ObjectOutputStream> out = null;
    private int clientCount; //The number of clients before game starts
	
    public SynchronizeProjectileThread(int clientCount, List<ObjectOutputStream> out){
       this.clientCount = clientCount;
       this.out = out; 
    }

	@Override
	public void run() {
		while(true){
            try{
            	// Only send to sockets (clients) we know of.
            	for (ObjectOutputStream outStream: this.out) {
            		MPacket tick = new MPacket("SYNC", MPacket.ACTION, MPacket.MISSILE_TICK);
            		outStream.writeObject(tick);
            	}
                Thread.currentThread().sleep(200);
            }catch(Exception e){
            }
        }
		
	}

}
