import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    
	//The maximum of clients that will join
	//Server waits until the max number of clients to join 
    private static final int MAX_CLIENTS = 2;
    private ServerSocket serverSocket = null;
    private BlockingQueue eventQueue = null; //A list of events
    private Socket socket = null; 
    private List<ObjectOutputStream> out = null;
    private int clientCount; //The number of clients before game starts
    private List<Socket> socketList = null; //A list of MSockets
    private List<String> clientList = null;
    private List<Player> playerList = null;
    public AtomicBoolean oneTime;
    
    
    /*
    * Constructor
    */
    public Server(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        if(Debug.debug) System.out.println("Listening on port: " + port);
        eventQueue = new LinkedBlockingQueue<MPacket>();
        clientCount = 0;
        this.out = new ArrayList<ObjectOutputStream>();
        this.socketList = new ArrayList<Socket>();
        this.clientList = new ArrayList<String>();
        this.playerList = new ArrayList<Player>();
        this.oneTime = new AtomicBoolean(true);
    }
        
    /*
    *Starts the listener and sender threads 
    */
    public void startThreads() throws IOException{
    	
    	// Start a MISSILE TICK thread
    	new Thread(new SynchronizeProjectileThread(clientCount, out)).start();
    	
        //Listen for new clients always to support dynamic joins.
    	while(true) {
    		socket = serverSocket.accept();
    		clientCount++;
    		new Thread(new ServerListenerThread(socket, out, clientCount, socketList, clientList, playerList, oneTime)).start();
    	}  
    }

        
    /*
    * Entry point for server
    */
    public static void main(String args[]) throws IOException {
        if(Debug.debug) System.out.println("Starting the server");
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
                
        server.startThreads();    

    }
}
