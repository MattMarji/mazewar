import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientConnectionThread implements Runnable {
	
	MServerSocket mClientConnect = null;
	MSocket mSocket = null;

    public ClientConnectionThread(){

    }

    public void run() {
    	
    	// Each client runs a thread that listens for incoming connections. 
    	// Sit and accept ALL connections coming in from other clients.
       try {
		mClientConnect = new MServerSocket(4444);
		System.out.print("Listening for connections...");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
       //Listen for new clients always to support dynamic joins.
	   	while(true) {
	   		
	   		try {
				mSocket = mClientConnect.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		
	   		// We have a new connection to the client...
	   		System.out.println("Connected!");
	   	}
    }
    
}