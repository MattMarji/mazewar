import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class ClientConnectionThread implements Runnable {
	
	ServerSocket mClientConnect = null;
	MSocket mSocket = null;
	List<Player> playerList = null;
	List<Boolean> ackList = null;
	Hashtable<String, Client> clientTable = null;
	Maze maze = null;
	BlockingQueue eventQueue = null;
	String name = null;
	double RETRANSMISSION_TIMEOUT = 0;
	AtomicBoolean hasToken;
	
    public ClientConnectionThread(ServerSocket serverSocket, List<Player> playerList, Hashtable<String, Client> clientTable, Maze maze, BlockingQueue eventQueue, String name, AtomicBoolean hasToken){
    	this.mClientConnect = serverSocket;
    	this.playerList = playerList;
    	this.clientTable = clientTable;
    	this.maze = maze;
    	this.eventQueue = eventQueue;
    	this.name = name;
    	this.ackList = new ArrayList<Boolean>();
    	this.hasToken = hasToken;
    }

    public void run() {
    	for (Player player : playerList) {
    		if(player.name.equals(name)) {
    			ackList.add(true);
    		} else {
    			ackList.add(false);
    		}
    	}
       //Listen for new clients always to support dynamic joins.
	   	while(true) {
	   		
	   		try {
	   			Socket socket = mClientConnect.accept();
	   	    	mSocket = new MSocket(socket);
	   	    	
	   	    	// We start a thread to listen on this socket for incoming messages.
	   	    	new Thread(new ClientEventListenerThread(mSocket, playerList, ackList, clientTable, maze, eventQueue, name, hasToken)).start();
	   	    	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		
	   		// We have a new connection to the client...
	   		System.out.println("Connected!");
	   	}
    }
    
}