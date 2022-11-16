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

/**
 * creates all necessary sockets and threads for communicating
 * Keeps track of known neighbors
 * @author mellis
 *
 */
public class P2pServer 
{
	
	private ServerSocket server_main;

	private static ArrayList<String> neighbors = new ArrayList<String>();
	
	private TransactionPool pool;
	private Blockchain chain;
	

	/**
	 * constructs server thread
	 * @param chain
	 * @param pool
	 */
	public P2pServer(Blockchain chain, TransactionPool pool)
	{
		//neighbors.add("192.168.1.172");
		this.chain = chain;
		this.pool = pool;
		try {
			server_main = new ServerSocket(38013);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		ServerServing serve = new ServerServing(server_main,pool,this.chain, neighbors);
		new Thread(serve).start();
		
		
	}
	
	
	public ServerSocket get_server()
	{
		return server_main;
	}
	
	/**
	 * broadcast new chain to neighbors
	 */
	public void syncChain()
	{
		broadcastMessage(new Message(Message.Message_Type.REPLACE_CHAIN, chain.getChain(), null,null));
		
	}
	
	/**
	 * broadcasts data included in last block to remove from transaction pool
	 * @param data
	 */
	public void clearPool(ArrayList<Transaction> data)
	{
		broadcastMessage(new Message(Message.Message_Type.CLEAR_TRANS,null,null,data));
	}
	
	/**
	 * sends message to all known neighbors
	 * @param message
	 */
	public static void broadcastMessage(Message message) 
	{
		for(String addr: neighbors)
		{
			
			Socket s;
			try {
				s = new Socket(addr,38013);
			
			
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				
				out.writeObject(message);
				out.writeObject(new Message(Message.Message_Type.DONE,null,null,null));
				out.close();
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}


/**
 * server thread
 * @author mellis
 *
 */
class ServerServing implements Runnable
{
	private ServerSocket server_main;
	private ArrayList<String> neighbors;
	private Blockchain chain;
	private TransactionPool pool;
	private ArrayList<String> messages_received = new ArrayList<String>();
	
	public ServerServing(ServerSocket s,TransactionPool pool,Blockchain chain, ArrayList<String> n)
	{
		server_main = s;
		neighbors = n;
		this.chain = chain;
		this.pool = pool;
	}
	@Override
	public void run() 
	{
		
		while(true)
		{
			try
			{
				
				System.out.println("listening for connections...");
				Socket client = server_main.accept();
				System.out.println("server accepted client");
				String addr = client.getRemoteSocketAddress().toString().split(":")[0].substring(1);
				if(!neighbors.contains(addr))
					neighbors.add(addr);
				System.out.println(neighbors);
				Handler listener = new Handler(client,chain, pool,messages_received);
				
				
				
				new Thread(listener).start();
			
			}
			catch(Exception e)
			{
				System.out.println("there was an issue");
			}
		}
		
	}
	
}



/**
 * worker thread for message received
 * @author mellis
 *
 */
class Handler implements Runnable
{
	private ArrayList<String> messages_received;
	private Socket listener;
	private String name;
	private Blockchain chain;
	private TransactionPool pool;
	
	public Handler(Socket s, Blockchain bl, TransactionPool tp, ArrayList<String> mr)
	{
		messages_received = mr;
		listener = s;
		chain = bl;
		pool = tp;
	}
	
	
	
	@Override
	public void run() 
	{
		try
		{
					

			ObjectInputStream in = new ObjectInputStream(listener.getInputStream());
			Message message;
			
			
			
						
			while((message = (Message)(in.readObject())).type != Message.Message_Type.DONE)
			{
				if(!messages_received.contains(message.id))
				{
					messages_received.add(message.id);
					if(message.type == Message.Message_Type.CLEAR_TRANS)
						pool.clearPool(message.transactions);
					else if(message.type == Message.Message_Type.ADD_TRANS)
					{
						if(Transaction.verifyTransaction(message.transaction, chain, pool))
							pool.updateOrAddTransaction(message.transaction);
						
					}
					else if(message.type == Message.Message_Type.REPLACE_CHAIN)
						chain.replaceChain(message.chain);	
					
					P2pServer.broadcastMessage(message);
					
	//				else if(message.type == Message.Message_Type.CLEAR_TRANS)
	//					pool.clearPool(message.transactions);
				}
			}
			in.close();
			listener.close();
		}
		catch(Exception e)
		{
			System.out.println("there was an issue");
			e.printStackTrace();
		}
		
	}
	
	
}

