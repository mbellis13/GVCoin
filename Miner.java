import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import javax.swing.JLabel;


/**
 * establishes a miner
 * @author mellis
 *
 */
public class Miner 
{

	private Blockchain chain;
	private TransactionPool pool;
	private Wallet wallet;
	private P2pServer server;
	
	public Miner(Blockchain bc, TransactionPool tp, Wallet w, P2pServer p2p)
	{
		chain = bc;
		pool = tp;
		wallet = w;
		server = p2p;
	}
	
	public Block mine(FullNodeServer fns) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException, FileNotFoundException
	{
		
		Block block = chain.addBlock(pool,server,wallet,fns);
		server.syncChain();

		
		return block;
	}
	
	public TransactionPool getPool()
	{
		return pool;
	}
	
	public Blockchain getChain()
	{
		return chain;
	}
	
	public Wallet getWallet()
	{
		return wallet;
	}
}
