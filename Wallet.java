import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;



/**
 * individual Wallet which includes 
 * new address for each receiving transaction
 * @author mellis
 *
 */
public class Wallet 
{
	private ArrayList<KeyPair> outstandingPairs;
	private ArrayList<KeyPair> spentPairs;
	private ArrayList<Transaction> pending;


	
	
	/**
	 * constructs new wallet 
	 * @param fns current running full node 
	 * @throws NoSuchAlgorithmException
	 */
	public Wallet() throws NoSuchAlgorithmException
	{
		pending = new ArrayList<Transaction>();
		outstandingPairs = new ArrayList<KeyPair>();
		spentPairs = new ArrayList<KeyPair>();

		
		
	}
	
	/**
	 * reconstructs existing wallet
	 * @param os outstading key pairs
	 * @param fns
	 */
	public Wallet(ArrayList<KeyPair> os,ArrayList<KeyPair> s)
	{
		pending = new ArrayList<Transaction>();
		outstandingPairs = os;
		spentPairs = s;
	}
	

	/**
	 * adds an existing key pair to this wallet
	 * @param pub
	 * @param priv
	 * @throws FileNotFoundException
	 */
	public void addExistingPair(PublicKey pub, PrivateKey priv) throws FileNotFoundException
	{
		//No longer updates wallet text file
		boolean alreadyExists = false;
		for(KeyPair p : outstandingPairs)
		{
			if(p.getPublic().getEncoded().toString().equals(pub.getEncoded().toString()))
				alreadyExists = true;
		}
		if(!alreadyExists)	
			this.outstandingPairs.add(new KeyPair(pub,priv));
	}
	
	/**
	 * returns string representation of key pairs
	 * 
	 * @throws FileNotFoundException
	 */
	public String getWalletData() throws FileNotFoundException
	{
		String data = "";
		for(KeyPair key: outstandingPairs)
		{
			//data += "\n";
			data += "os key\n";
			data += Utility.privateKeyToAddress(key.getPrivate());
			data += "\n";
			data += Utility.publicKeyToAddress(key.getPublic());
			data += "\n";
		}
		for(KeyPair key: spentPairs)
		{
			data += "spent key\n";
			data += Utility.privateKeyToAddress(key.getPrivate());
			data += "\n";
			data += Utility.publicKeyToAddress(key.getPublic());
			data += "\n";
		}

		return data;
	}
	
	
	/**
	 * Creates a new Key Pair and adds to wallet
	 * @return
	 * @throws FileNotFoundException
	 */
	public KeyPair generateNewKey() throws FileNotFoundException
	{
		//no longer updates wallet text file
		
		KeyPairGenerator keyGen=null;
		try {
			keyGen = KeyPairGenerator.getInstance("EC");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyGen.initialize(256);
		KeyPair newpair = keyGen.generateKeyPair();
		//PrivateKey privKey = newpair.getPrivate();
		//PublicKey pubKey = newpair.getPublic();
		outstandingPairs.add(newpair);

		return newpair;
		
	}
	
	/**
	 * Creates a new outgoing transaction for this wallet 
	 * will utilize multiple key pairs if necessary
	 * @param destAddr
	 * @param bc
	 * @param pool
	 * @param amt
	 * @return
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeySpecException 
	 */
	public Transaction generateTransactions(String destAddr,Blockchain bc, TransactionPool pool, double amt) throws InvalidKeySpecException, NoSuchProviderException
	{	
		Input input = null;
		ArrayList<Output> outputs = new ArrayList<Output>();
		ArrayList<KeyPair>keysForInputs = new ArrayList<KeyPair>();
		//ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		double lastAmt = 0;
		double totalSending = amt;
		
		for(int i = 0; i < outstandingPairs.size(); i++)
		{
			KeyPair key = outstandingPairs.get(i);
			double remaining = bc.getKeyBalance(pool, key.getPublic());//****how to get complete key balance??
			
			//if the balance of this key is sufficient to finish transaction
			if(totalSending <= remaining)
			{
				keysForInputs.add(key);
				lastAmt = totalSending;
				Output op = new Output(Utility.publicKeyToAddress(key.getPublic()),0);
				
				outputs.add(op);
				PublicKey newKey = null;
				try {
					newKey = generateNewKey().getPublic();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				String changeAddr = Utility.publicKeyToAddress(newKey);
				Output change = new Output(changeAddr,remaining-lastAmt);
				outputs.add(change);
				break;
			}
			else //use all of this key but need more keys
			{
				keysForInputs.add(key);
				totalSending-=remaining;
				Output op = new Output(Utility.publicKeyToAddress(key.getPublic()),0);
				outputs.add(op);
				
				spentPairs.add(key);
				
			}
		}
		for(KeyPair key: spentPairs)
			outstandingPairs.remove(key);
		
		Output op1 = new Output(destAddr, amt);
		outputs.add(op1);
		
		for(KeyPair key: keysForInputs)
		{	
			
			try {
				input = Transaction.generateInput(key, outputs, bc.getKeyBalance(pool, key.getPublic())); 
				
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			
		}
		
		Transaction newTransaction = new Transaction(input,outputs);
		pending.add(newTransaction);
		return newTransaction;
	}
	

	/**
	 * Creates a signature
	 * @param dataHash
	 * @param publicKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public String sign(String dataHash,PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
		PrivateKey privateKey = null;
		for(KeyPair p : this.outstandingPairs)
		{
			if(p.getPublic().getEncoded().toString().equals(publicKey.getEncoded().toString()))
				privateKey = p.getPrivate();
		}
		if(privateKey == null)
			return null;
		Signature signer = Signature.getInstance("SHA1WithECDSA");
		signer.initSign(privateKey);
		signer.update(dataHash.getBytes("UTF8"));
		return Utility.toHexString(signer.sign());
		
	}
	
	
	/**
	 * returns all outstanding public keys for this wallet
	 * @return
	 */
	public ArrayList<PublicKey> getPublicKeys()
	{
		ArrayList<PublicKey> keys = new ArrayList<PublicKey>();
		for(KeyPair p : this.outstandingPairs)
			keys.add(p.getPublic());
		return keys;
	}
	

	
	/**
	 * removes transaction from pending if included on blockchain
	 * with at least 5 blocks on top of it.  
	 * 
	 * @param chain
	 */
	public void checkPending(Blockchain chain)
	{
		ArrayList<Block> blocks = chain.getChain();
		for(int i = 0; i < pending.size(); i++)
		{
			for(int j = blocks.size()-1; j >=0; j--)
			{
				ArrayList<Transaction> transactions = blocks.get(0).getData();
				for(Transaction t: transactions)
				{
					if(pending.get(i).getID().equals(t.getID())
							&& blocks.size() - j > 5)
					{
						pending.remove(i);
						i--;
					}
				}
				
			}
		}
	}
	
	
	/**
	 * calculates cumulative balance for all outstanding keys
	 * associated with this wallet
	 * @param bc
	 * @param pool
	 * @return
	 */
	public double calculateBalance(Blockchain bc, TransactionPool pool)
	{
		double balance = 0;
		for(KeyPair p : this.outstandingPairs)
		{
			PublicKey publicKey = p.getPublic();
			balance += bc.getKeyBalance(pool,publicKey);
		}
		return balance;
	}
}


