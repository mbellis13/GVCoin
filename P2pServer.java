import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * creates all necessary sockets and threads for communicating
 * Keeps track of known neighbors
 * @author mellis
 *
 */
public class P2pServer 
{
	
	//use Queues in order to process messages
	
	
	
	/**
	 * constructs all sender threads and adds messages for processing
	 * @param chain
	 * @param pool
	 */
	public P2pServer(List<Message> incomingQueue, List<Message> outgoingQueue)
	{

		ArrayList<String> neighbors = new ArrayList<String>();
		neighbors.add("127.0.0.1");
		
		
		
		MessageSender[] senders = new MessageSender[4];
		for(int i = 0; i < 4; i++)
		{
			senders[i] = new MessageSender(neighbors, outgoingQueue);
			new Thread(senders[i]).start();
		}
		
		
		
		
		
		
		
	}
}

class ServerServing implements Runnable
{
	
	private List<Message> incomingQueue;
	private List<Message> outgoingQueue;
	private ArrayList<String> neighbors;
	
	public ServerServing(List<Message> incomingQueue, List<Message> outgoingQueue, ArrayList<String> neighbors)
	{
		this.incomingQueue = incomingQueue;
		this.outgoingQueue = outgoingQueue;
		neighbors = neighbors;
	}
	@Override
	public void run() {
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(38013);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		ArrayList<String> receivedIds = new ArrayList<String>();
		while(true)
		{
			
			
			
			
			try
			{
				
				System.out.println("listening for connections...");
				Socket client = serverSocket.accept();
				System.out.println("server accepted client");
				String addr = client.getRemoteSocketAddress().toString().split(":")[0].substring(1);
				if(!neighbors.contains(addr))
				{
					neighbors.add(addr);
				}

				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				
				Message message = (Message)(in.readObject());
				if(!receivedIds.contains(message.getId()))
					receivedIds.add(message.getId());
				synchronized (incomingQueue)
				{
					incomingQueue.add(message);
				}
				incomingQueue.notifyAll();
			
			}
			catch(Exception e)
			{
				System.out.println("there was an issue");
				try {
					serverSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
	}
	
}




/**
 * class for implementing sending worker threads
 * @author mellis
 *
 */

class MessageSender implements Runnable
{
	private List<Message> queue;
	private ArrayList<String> neighbors;
	
	public MessageSender(ArrayList<String> neighbors, List<Message> queue)
	{
		this.queue = queue;
		this.neighbors = neighbors;
	}
	
	public void run()
	{
		while(true)
		{
			Message message = null;
			synchronized (queue)
			{
				while(queue.size() == 0)
				{
					try {
						queue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				message = queue.remove(0);
			}
			
			broadcastMessage(message);
		}
	}
	
	
	
	/**
	 * sends message to all known neighbors
	 * @param message
	 */
	public void broadcastMessage(Message message) 
	{
		for(String addr: neighbors)
		{
			Socket s;
			try {
				s = new Socket(addr,38013);
			
			
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				
				out.writeObject(message);
				//out.writeObject(new Message(Message.Message_Type.DONE,null,null,null));
				out.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	
}



