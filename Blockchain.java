import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;


/**
 * Full Blockchain
 * @author mellis
 *
 */
public class Blockchain implements Serializable
{
	private static final long serialVersionUID = 3313131331190761313L;
	
	
	public static int difficulty = 4;
	public static Block lastBlock = Block.genesis();
	public static long MINE_RATE = 60000;
	public static double MINING_REWARD = 50;
	
	
	private ArrayList<Block> chain;
	
	/**
	 * constructs new Blockchain
	 */
	public Blockchain()
	{
		chain = new ArrayList<Block>();
		chain.add(Block.genesis());
	}
	
	/**
	 * constructs existing blockchain from list of blocks
	 * @param b
	 */
	public Blockchain(ArrayList<Block> b)
	{
		chain = b;
	}
	
	/**
	 * returns the last block in chain
	 * @return
	 */
	public Block getLastBlock()
	{
		return chain.get(chain.size()-1);
	}
	
	/**
	 * writes the blockchain object to blockchain.dat for 
	 * reconstruction on next run of program
	 */
	public void backupChain()
	{
		try {
			FileOutputStream fos = new FileOutputStream("blockchain.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);

			
			oos.close();
		}
		catch(IOException ex)
		{
			System.out.println("error writing chain");
		}
	}
	
	/**
	 * returns the last output for given address
	 * @param pubKey
	 * @return
	 */
	public double getLastOutputBalance(String pubKey)
	{
		for(int i = chain.size()-1; i >=0; i--)
		{
			
			if(chain.size()>1)
			{
				
				Block b = chain.get(i);
				
				for(Transaction t : b.getData()) 
				{
					for(Output m: t.getOutputs())
					{
						
						if(m.getAddress().equals(pubKey))
						{
	
							return m.getAmount();
						}
					}
				}
			}
		}

		return 0;
	}
	
	

	
	
	
	/**
	 * returns the current balance for given key including pending transactions
	 */
	public double getKeyBalance(TransactionPool pool, PublicKey publicKey)
	{
		//THIS REALLY NEEDS TO BE LOOKED AT.   NO VERIFICATION OF TRANSACTIONS IN POOL
		//ArrayList<Block> chain = this.getChain();
		//ArrayList<Transaction> relevant = new ArrayList<Transaction>();
		double amt = 0;
		String keyString = Utility.publicKeyToAddress(publicKey);
		amt = this.getLastOutputBalance(keyString);

		
		pool.sortByTimestamp();
		ArrayList<Transaction> transactions = pool.getTransactionsFor(keyString);
		for(Transaction t: transactions)
		{	
			
			for(Output output: t.getOutputs())
			{
				if(output.getAddress().equals(keyString))
					amt = output.getAmount();
			}
		}

	
		return amt;
	}
	

	
	/**
	 * validates a blockchain
	 * @param chain
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private boolean isValidChain(ArrayList<Block> chain) throws NoSuchAlgorithmException
	{
		if(!chain.get(0).toString().equals(Block.genesis().toString()))
		{
			System.out.println("genesis incorrect");
			return false;
		}
		for(int i = 1; i < chain.size(); i++)
		{
			Block block = chain.get(i);
			Block lastBlock = chain.get(i-1);
			if(!block.getLastHash().equals(lastBlock.getHash()) || !block.getHash().equals(blockHash(block)))
			{
				return false;
			}
			if(block.getTime() - lastBlock.getTime() < MINE_RATE 
					&& block.getDifficulty() != lastBlock.getDifficulty()+1)
				return false;
			if(block.getTime() - lastBlock.getTime() > MINE_RATE 
					&& block.getDifficulty() != lastBlock.getDifficulty()-1)
				return false;
		}
		return true;
	}
	
	
	/**  
	 * replaces current chain with new chain if valid
	 * @param newChain
	 * @throws NoSuchAlgorithmException
	 */
	public void replaceChain(ArrayList<Block> newChain) throws NoSuchAlgorithmException
	{
		if(newChain.size() <= chain.size())
		{
			System.out.println("new chain rejected, size not larger");
			return;
		}
		else if(!isValidChain(newChain))
		{
			System.out.println("new chain rejected, invalid");
			return;
		}
		
		System.out.println("new chain accepted");
		this.chain = newChain;	
		backupChain();
	}
	
	
	
	public void addBlock(Block block)
	{
		chain.add(block);
		try {
			if(!this.isValidChain(chain))
				chain.remove(block);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * calculates hash for a given block
	 * @param b
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private String blockHash(Block b) throws NoSuchAlgorithmException
	{
		String input = "" +b.getTime() +b.getLastHash() + b.getData() + b.getNonce() + b.getDifficulty();
		return Utility.toHexString(Utility.getSHA(input));
	}
	
	
	
	/**
	 * returns blockchain as a string
	 * @return
	 */
	public String stringChain()
	{
		String resp = "******** CURRENT CHAIN ********\n";
		for(int i = 0; i < chain.size(); i++)
		{
			resp += chain.get(i) + "\n";
		}
		return resp;
	}
	
	
	
	/**
	 * returns the blocks in the chain
	 * @return
	 */
	public ArrayList<Block> getChain()
	{
		return chain;
	}

}
