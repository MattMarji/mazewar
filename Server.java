import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    
	//The maximum of clients that will join
	//Server waits until the max number of clients to join 
    private static final int MAX_CLIENTS = 2;
    private ServerSocket serverSocket = null;
    private int clientCount; //The number of clients before game starts
    private List<Socket> socketList = null; //A list of MSockets
    private List<String> clientList = null;
    private BlockingQueue eventQueue = null; //A list of events
    private Socket socket = null; 
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    
    /*
    * Constructor
    */
    public Server(int port) throws IOException{
        clientCount = 0; 
        serverSocket = new ServerSocket(port);
        if(Debug.debug) System.out.println("Listening on port: " + port);
        socketList = null;
        clientList = null;
        eventQueue = new LinkedBlockingQueue<MPacket>();
    }
    
    /*
    *Starts the listener and sender threads 
    */
    public void startThreads() throws IOException{
    	
    	socket = serverSocket.accept();
        
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    	
        //Listen for new clients always to support dynamic joins.
        while(true){
            //Start a new listener thread for each new client connection
            
            
            MPacket received = null;
            
			try {
				received = (MPacket) in.readObject();
				//so we've now pulled the incoming data from the socket and stream, we need to store it in the ingress queue. 
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            if(Debug.debug) System.out.println("Received: " + received);
            
            String incomingName = received.name;
            
            if(clientList.contains(incomingName)) {
            //Check that there isn't already a client with this name in our game, because all clients need to have unique names.

            	System.out.println("A client with the name: " + incomingName + " already exists. Please select another name.");
            } else {
                clientList.add(incomingName);
                clientCount++;
            	System.out.println("Client: " + incomingName + " added to the game, there are now " + clientCount + " clients.");
            	socketList.add(socket);
            	//Here we need to broadcast the client list to everyone. 
            	for (Socket sock : socketList) {
            		socket.(clientList);
            	}
            }
            
            
            new Thread(new ServerListenerThread(mSocket, eventQueue)).start();
            
                                        
            
            
        }
        
        //Start a new sender thread 
        new Thread(new ServerSenderThread(mSocketList, eventQueue)).start();    
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
