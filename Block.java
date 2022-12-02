import java.io.Serializable;
import java.util.ArrayList;




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
	
	@Override
	public boolean equals(Object otherObject)
	{
		if(! (otherObject instanceof Block))
			return false;
		
		Block other = (Block) otherObject;
		
		return (this.timestamp == other.timestamp) &&
				(this.lastHash.equals(other.lastHash)) &&
				(this.hash.equals(other.hash)) &&
				(this.data.toString().equals(other.data.toString())) &&
				(this.nonce == other.nonce)&&
				(this.difficulty == other.difficulty);
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
