import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TransactionPool 
{
	private ArrayList<Transaction> pool;
	
	public TransactionPool()
	{
		pool = new ArrayList<Transaction>();
	}
	

	
	
	/**
	 * looks for an existing transaction in the pool for
	 * the sender address
	 * 
	 * @param address sender's address
	 * @return the transaction found or null if none found
	 */
	public Transaction findExistingTransaction(String address)
	{
		
		for(Transaction t : pool)
		{
			if(t.getInput().size() == 1 &&
			t.getInput().get(0).get("address").equals(address))
				return t;
		}
		return null;
	}
	
	public int findExistingTransactionID(Transaction newTrans) 
	{
		for(int i = 0; i < pool.size(); i++)
		{
			Transaction t = pool.get(i);
			if (t.getID().equals(newTrans.getID()))
				return i;
		}
		return -1;
	}
	
	
	/**
	 * adds a new transaction or updates an existing transaction
	 * 
	 * @param newTrans new transaction to be added
	 */
	public void updateOrAddTransaction(Transaction newTrans)
	{

		int existing = findExistingTransactionID(newTrans);
		
		if(existing > -1)
		{
			pool.set(existing, newTrans);
		}
		else
			pool.add(newTrans);
	}
	
	
	/**
	 * returns list of valid transactions from transaction pool
	 * @param chain
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	public ArrayList<Transaction> validTransactions(Blockchain chain) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException
	{
		this.sortByTimestamp();
		ArrayList<Transaction> valid = new ArrayList<Transaction>();
		for(Transaction t : pool)
		{
			if(Transaction.verifyTransaction(t,chain,this))
				valid.add(t);
			else
				System.out.println("invalid transaction: " + t.getID());
		}
		
		return valid;
	}
	
	/**
	 * removes transactions from pool which have been included in a block
	 * @param data
	 */
	public void clearPool(ArrayList<Transaction> data)
	{	
		for(int i = 0; i < data.size(); i++)
		{
			for(int j = 0; j < pool.size(); j++)
				if(data.get(i).toString().equals(pool.get(j))) 
					pool.remove(data.get(i));
		}
		System.out.println("transaction pool cleared");
	}
	
	
	/**
	 * returns transactions involving given address
	 * @param from
	 * @return
	 */
	public ArrayList<Transaction> getTransactionsFor(String from)
	{
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		
		for(Transaction t: pool)
		{	
			System.out.println(t);
			boolean add = false;
			for(HashMap output: t.getOutputs())
			{
				if(output.get("address").equals(from))
					add = true;
			}
			if(add) 
				transactions.add(t);
		}
		
		return transactions;
	}
	
	/**
	 * sorts this Transaction Pool by first input timestamp
	 */
	public void sortByTimestamp()
	{

		for(int i = 0; i < pool.size()-1; i++)
		{
			int min = i;
			for(int j = i+1; j < pool.size(); j++)
			{
				Transaction t1 = pool.get(min);
				Transaction t2 = pool.get(j);
				long ts1= (Long) (t1.getInput().get(0).get("timestamp"));
				long ts2 = (Long) (t2.getInput().get(0).get("timestamp"));
				
				
				HashMap inputs= t1.getInput().get(0);
				ts1 = (Long)(inputs.get("timestamp"));
				
				HashMap inputs2= t2.getInput().get(0);
				ts2 = (Long)(inputs2.get("timestamp"));
				
				if(ts2 < ts1)
				{
					min = j;
				}
			}
			Collections.swap(pool, i, min);
		}
	}
	
	public String toString()
	{
		String resp = "CURRENT TRANSACTION POOL";
		for(Transaction t : pool)
		{
			resp += "\n\t" + t;
		}
		return resp;
	}
}
