
import java.util.ArrayList;
import java.util.Arrays;


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
			if(t.getInput().getAddress().equals(address))
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
	
	
	
//	public ArrayList<Transaction> getValidTransactions(Blockchain chain)
//	{
//		this.sortByTimestamp();
//		ArrayList<Transaction> valid = new ArrayList<Transaction>();
//		for(Transaction t : pool)
//		{
//			if(Transaction.verifyTransaction(t,chain,this))
//				valid.add(t);
//			else
//				System.out.println("invalid transaction: " + t.getID());
//		}
//		
//		return valid;
//	}
	
	
	
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
			boolean add = false;
			for(Output output: t.getOutputs())
			{
				if(output.getAddress().equals(from))
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
		
		Transaction[] transactions = new Transaction[pool.size()];
		transactions = pool.toArray(transactions);
		mergeSortByTimestamp(transactions);
		pool = new ArrayList<Transaction>(Arrays.asList(transactions));
		
	}
	
	
	/**
	 * helper method to sort by timestamp
	 * @param list
	 */
	private void mergeSortByTimestamp(Transaction[] list)
	{
		if(list.length <= 1)
			return;
		Transaction[] left = new Transaction[list.length/2];
		Transaction[] right = new Transaction[list.length - left.length];
		
		for(int i = 0; i < left.length; i++)
		{
			left[i] = list[i];
			right[i] = list[left.length + i];
		}
		right[right.length - 1] = list[list.length -1];
		
		mergeSortByTimestamp(left);
		mergeSortByTimestamp(right);
		
		int leftIndex = 0;
		int rightIndex = 0;
		
		for(int i = 0; i < list.length; i++)
		{
			if(leftIndex >= left.length)
			{
				list[i]=right[rightIndex];
				rightIndex++;
			}
			else if(rightIndex >= right.length)
			{
				list[i] = left[leftIndex];
				leftIndex++;
			}
			else if(left[leftIndex].getTimestamp()<=right[rightIndex].getTimestamp())
			{
				list[i]=left[leftIndex];
				leftIndex++;
			}
			else
			{
				list[i] = right[rightIndex];
				rightIndex++;
			}
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




	public ArrayList<Transaction> getPool() {
		return pool;
	}
}
