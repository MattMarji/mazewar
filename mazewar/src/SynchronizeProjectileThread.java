import java.lang.Thread;
import java.util.concurrent.BlockingQueue;

public class SynchronizeProjectileThread implements Runnable  {
	
	
	private BlockingQueue eventQueue = null;
    
    public SynchronizeProjectileThread(BlockingQueue eventQueue){
        this.eventQueue = eventQueue;
    }

	@Override
	public void run() {
		while(true){
            try{
                eventQueue.put(new MPacket("SYNC", MPacket.ACTION, MPacket.BULLET));
                Thread.currentThread().sleep(200);
            }catch(Exception e){
            }
        }
		
	}

}
