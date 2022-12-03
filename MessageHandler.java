import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;



/**
 * worker thread for message received
 * @author mellis
 *
 */
class MessageHandler 
{

	private Handler[] workers;
	
	
	/**
	 * create worker threads to process incoming messages
	 * @param bl  active Blockchain object
	 * @param pool  active TransactionPool object
	 * @param numWorkers   number of worker threads
	 */
	public MessageHandler(Blockchain bl, TransactionPool pool, int numWorkers, List<Message> incomingQueue, List<Message> outgoingQueue)
	{

		workers = new Handler[numWorkers];
		
		for(int i = 0; i < numWorkers; i++)
		{
			workers[i] = new Handler(bl, pool,incomingQueue, outgoingQueue);
			new Thread(workers[i]).start();
		}
		
	}
	
}


 /**
  * Runnable class for processing incoming messages
  * @author mellis
  *
  */
class Handler implements Runnable
{

	private Blockchain blockchain;
	private TransactionPool pool;
	private List<Message> incomingQueue;
	private List<Message> outgoingQueue;
	
	public Handler(Blockchain blockchain, TransactionPool pool, List<Message> incomingQueue, List<Message> outgoingQueue)
	{
		this.incomingQueue = incomingQueue;
		this.blockchain = blockchain;
		this.pool = pool;
		this.outgoingQueue = outgoingQueue;
	}
	
	@Override
	public void run() 
	{
		try
		{
					
			Message message;
			
			while(true)
			{
				
				synchronized(incomingQueue)
				{
					while(incomingQueue.size() == 0)
					{
						this.incomingQueue.wait();
					}
					
					message = incomingQueue.get(0);
				}
				
				Message.Type type = message.getType();
				
				
				switch (type)
				{
				case CLEAR_TRANS:
					synchronized(pool)
					{
						pool.clearPool(message.getTransactions());
					}
					break;
					
				case ADD_TRANS:
					synchronized (pool)
					{
						if(message.getTransaction().verifyTransaction(blockchain, pool))
						{
							pool.updateOrAddTransaction(message.getTransaction());
						}
						
					}
					break;
					
				case REPLACE_CHAIN:
					synchronized(blockchain)
					{
						blockchain.replaceChain(message.getChain());	
					}
					break;
					
				case REQUEST_CHAIN:
					synchronized(outgoingQueue)
					{
						outgoingQueue.add(new Message(Message.Type.REPLACE_CHAIN,blockchain.getChain(),null,null));
					}
					break;
					
				}
				

				if(message.getType() != Message.Type.REQUEST_CHAIN)
				{
					synchronized(outgoingQueue)
					{
						outgoingQueue.add(message);
						outgoingQueue.notifyAll();
					}
					
				}
				
				
				
			}
			
		}
		catch(Exception e)
		{
			System.out.println("there was an issue");
			e.printStackTrace();
		}
		
	}
	
}
