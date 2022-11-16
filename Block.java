import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Single Block
 * Data is unlimited number of transactions
 * @author mellis
 *
 */
public class Block implements Serializable
{
	private static final long serialVersionUID = 40513111313131313L;
	
	private long timestamp;
	private String lastHash;
	private String hash;
	private ArrayList<Transaction> data;
	private long nonce;
	private int difficulty;
	
	
	/**
	 * Constructs a Block
	 * @param ts  timestamp
	 * @param lh  last hash
	 * @param h	 hash
	 * @param data  transactions included
	 * @param n  nonce
	 * @param d  difficulty
	 */
	public Block(long ts, String lh, String h, ArrayList<Transaction> data, long n, int d)
	{
		timestamp = ts;
		lastHash = lh;
		hash = h;
		this.data = data;
		nonce = n;
		difficulty = d;
	}
	
	
	/**
	 * Constructs genesis block
	 * @return
	 */
	public static Block genesis() {
		return new Block(1622639694160L,"ThisIsTheGenesisBlock","f1r5tb10ck",new ArrayList<Transaction>(),0,4);
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
	public static Block mineBlock(Blockchain bc, TransactionPool pool,P2pServer p2p,
			Wallet w, FullNodeServer fns) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException, FileNotFoundException {
		String hash;
		long timestamp =0;
		Block lastBlock = null;
		String lastHash = null;
		int difficulty=4;
		ArrayList<Transaction> data = null;
		long nonce = 0;
		Date d = new Date();
		data = pool.validTransactions(bc);
		data.add(Transaction.miningRewardTransaction(w));
		lastBlock = bc.getLastBlock();
		lastHash = lastBlock.getHash();
		do {
			nonce++;

			if(!bc.getLastBlock().toString().equals(lastBlock.toString()))
				{System.out.print("beat to the punch");return null;}

			timestamp = d.getTime();
			difficulty = Block.adjustDifficulty(lastBlock,timestamp);
			hash = Block.hash(timestamp, lastHash, data, nonce, difficulty);
			
		}while(!hash.substring(0,difficulty).equals(new String(new char[difficulty]).replace("\0","a"))&&
				fns.getMining());
		if(!fns.getMining())
			return null;
		pool.clearPool(data);
		p2p.clearPool(data);
		System.out.println("NEW BLOCK UNLOCKED: DIFFICULTY: " + difficulty + " time: " + timestamp);
		return new Block(timestamp, lastHash, hash, data, nonce, difficulty);
	}
	
	/**
	 * sets current difficulty
	 * @param lastBlock
	 * @param currentTime
	 * @return
	 */
	public static int adjustDifficulty(Block lastBlock, long currentTime)
	{
		int diff = lastBlock.getDifficulty();
		return lastBlock.getTime() + Blockchain.MINE_RATE > currentTime ? diff + 1 : diff -1;
	}
	
	
	/**
	 * calculates hash for a given block
	 * @param b
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String blockHash(Block b) throws NoSuchAlgorithmException
	{
		return hash(b.getTime(), b.getLastHash(),b.getData(), b.getNonce(), b.getDifficulty());
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
	public static String hash(long ts, String lh, ArrayList<Transaction> data, long n, int d) throws NoSuchAlgorithmException
	{
		String input = "" + ts + lh + data + n + d;
		return Utility.toHexString(Utility.getSHA(input));
	}
	
	
	

	
	
	public String getLastHash()
	{
		return lastHash;
	}
	
	public ArrayList<Transaction> getData()
	{
		return data;
	}
	
	public long getNonce()
	{
		return nonce;
	}
	public long getTime()
	{
		return timestamp;
	}
	
	public String getHash()
	{
		return hash;
	}
	
	public int getDifficulty()
	{
		return difficulty;
	}
	
	public String toString()
	{
		String resp = "\nBLOCK:\n\ttimestamp: " + timestamp  + 
				"\n\tlastHash  : "+ lastHash + 
				"\n\tHash      : "+ hash + 
				"\n\tdata      : "+data + 
				"\n\tnonce     : "+nonce + 
				"\n\tdifficulty: "+difficulty;
		return resp;
	}

}
