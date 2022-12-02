import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;


/**
 * establishes a miner
 * @author mellis
 *
 */
public class Miner 
{

	private boolean stopMining;

	
	
	public Miner()
	{
		stopMining = false;

	}
	
	
	
	/**
	 * Calculates hashes to unlock a block
	 * @param bc  blockchain
	 * @param pool  transaction pool
	 * @param p2p  Peer to peer server
	 * @param w  current user's wallet
	 * @param fns  current FullNodeServer running
	 * @return  returns the unlocked block
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 * @throws FileNotFoundException
	 */
	public Block mineBlock(Blockchain chain, KeyPair key, ArrayList<Transaction> data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException, FileNotFoundException {
		
		//may want to remove mining manipulation to main class
		//did not clear pool or send messages
		

		String hash;
		long timestamp =0;
		Block lastBlock = null;
		String lastHash = null;
		int difficulty=4;
		long nonce = 0;
		Date d = new Date();
		data.add(Transaction.getMiningRewardTransaction(Utility.publicKeyToAddress(key.getPublic())));
		lastBlock = chain.getLastBlock();
		lastHash = lastBlock.getHash();
		do {
			nonce++;

			if(!chain.getLastBlock().toString().equals(lastBlock.toString()))
			{
				return null;
			}

			timestamp = d.getTime();
			difficulty = Block.adjustDifficulty(lastBlock,timestamp);
			hash = getHash(timestamp, lastHash, data, nonce, difficulty);
			
		}while(!hash.substring(0,difficulty).equals(new String(new char[difficulty]).replace("\0","a"))&&
				!stopMining);
		if(stopMining)
		{
			stopMining = false;
			return null;
		}
			
		
		System.out.println("NEW BLOCK UNLOCKED: DIFFICULTY: " + difficulty + " time: " + timestamp);
		return new Block(timestamp, lastHash, hash, data, nonce, difficulty);
	}
	
	/**
	 * calculates hash for a given set of data
	 * @param ts timestamp
	 * @param lh last hash
	 * @param data List of transactions
	 * @param n nonce
	 * @param d difficulty
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String getHash(long ts, String lh, ArrayList<Transaction> data, long n, int d) throws NoSuchAlgorithmException
	{
		String input = "" + ts + lh + data + n + d;
		return Utility.toHexString(Utility.getSHA(input));
	}
	
	

}
