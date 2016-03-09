import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientConnectionThread implements Runnable {
	
	ServerSocket mClientConnect = null;
	MSocket mSocket = null;

    public ClientConnectionThread(ServerSocket serverSocket){
    	this.mClientConnect = serverSocket;
    }

    public void run() {
    	       
       //Listen for new clients always to support dynamic joins.
	   	while(true) {
	   		
	   		try {
	   			Socket socket = mClientConnect.accept();
	   	    	mSocket = new MSocket(socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		
	   		// We have a new connection to the client...
	   		System.out.println("Connected!");
	   	}
    }
    
}