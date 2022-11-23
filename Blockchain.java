import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;


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
					for(HashMap<String, String> m: t.getOutputs())
					{
						
						if(m.get("address").equals(pubKey))
						{
	
							return Double.parseDouble(m.get("amount"));
						}
					}
				}
			}
		}

		return 0;
	}
	
	
	public double getKeyBalanceForVerification(TransactionPool pool, PublicKey publicKey, Transaction trans)
	{
		
		pool.sortByTimestamp();
		ArrayList<Block> chain = this.getChain();
		String keyString = Utility.publicKeyToAddress(publicKey);
		ArrayList<Transaction> relevant = pool.getTransactionsFor(keyString);
		

		double amt = 0;
		amt = this.getLastOutputBalance(keyString);



		//TRANSACTION POOL SORTED, CALCULATE FROM HERE
		
		for(int i = 0; i< relevant.size(); i++)
		{
			Transaction check = relevant.get(i);
			if(check.getID().equals(trans.getID()))
				break;
			
			for(HashMap<String, String> output: check.getOutputs())
			{
				System.out.println(trans);
				
				if(output.get("address").equals(Utility.publicKeyToAddress(publicKey)))
					amt = Double.parseDouble(output.get("amount"));
			}
			
		}
		
		return amt;
	}
	
	
	
	/**
	 * returns the current balance for given key including pending transactions
	 */
	public double getKeyBalance(TransactionPool pool, PublicKey publicKey)
	{
		//THIS REALLY NEEDS TO BE LOOKED AT.   NO VERIFICATION OF TRANSACTIONS IN POOL
		ArrayList<Block> chain = this.getChain();
		ArrayList<Transaction> relevant = new ArrayList<Transaction>();
		double amt = 0;
		String keyString = Utility.publicKeyToAddress(publicKey);
		amt = this.getLastOutputBalance(keyString);
		//System.out.println("last output: " + amt);
		
		pool.sortByTimestamp();
		ArrayList<Transaction> transactions = pool.getTransactionsFor(keyString);
		long currentTS = 0;
		for(Transaction t: transactions)
		{	
			for(HashMap<String, String> output: t.getOutputs())
			{
				if(output.get("address").equals(keyString))
					amt = Double.parseDouble(output.get("amount"));
			}
		}
			//System.out.println("***********\n" + t);
//			for(HashMap<String, String> input: t.getInput())
//			{
//				//System.out.println("##########\n" + input);
//				if( input.get("address").equals(keyString) && (Long)(input.get("timestamp"))>currentTS)
//				{
//					currentTS = (Long)(input.get("timestamp"));
//					//						if(t.verifyTransaction(t, this, pool))
////						{
//					for(HashMap<String, String> m : t.getOutputs())
//					{
//						if(m.get("address").equals(keyString))
//						{
//							System.out.println(m.get("amount"));
//							amt = (Double)(m.get("amount"));
//						}
//							
//					}
//				}
//			}
//		}
	
		return amt;
	}
	
	/**
	 * attempts to mine a block and add to the chain
	 * @param pool current transaction pool
	 * @param p2p peer 2 peer server
	 * @param w current user's wallet
	 * @param fns current running full node
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 * @throws FileNotFoundException
	 */
	public Block addBlock(TransactionPool pool, P2pServer p2p,
			Wallet w,FullNodeServer fns) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException, FileNotFoundException
	{
		Block b = Block.mineBlock(this, pool,p2p,w,fns);
		if(b != null)
			{
			chain.add(b);
			}
		//System.out.print(chain);
		backupChain();
		return b;
	}
	
	/**
	 * validates a blockchain
	 * @param chain
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public boolean isValidChain(ArrayList<Block> chain) throws NoSuchAlgorithmException
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
			if(!block.getLastHash().equals(lastBlock.getHash()) || !block.getHash().equals(Block.blockHash(block)))
			{
				return false;
			}
			if(block.getTime() - lastBlock.getTime() < this.MINE_RATE 
					&& block.getDifficulty() != lastBlock.getDifficulty()+1)
				return false;
			if(block.getTime() - lastBlock.getTime() > this.MINE_RATE 
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
