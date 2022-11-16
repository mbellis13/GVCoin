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
import java.util.HashMap;


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
	private double balance;
	private FullNodeServer fullNode;

	
	
	/**
	 * constructs new wallet 
	 * @param fns current running full node 
	 * @throws NoSuchAlgorithmException
	 */
	public Wallet(FullNodeServer fns) throws NoSuchAlgorithmException
	{
		pending = new ArrayList<Transaction>();
		balance = 0;
		outstandingPairs = new ArrayList<KeyPair>();
		spentPairs = new ArrayList<KeyPair>();
		fullNode = fns;
		
		
	}
	
	/**
	 * reconstructs existing wallet
	 * @param os outstading key pairs
	 * @param fns
	 */
	public Wallet(ArrayList<KeyPair> os,ArrayList<KeyPair> s, FullNodeServer fns)
	{
		pending = new ArrayList<Transaction>();
		balance = 0;
		outstandingPairs = os;
		fullNode = fns;
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
		boolean alreadyExists = false;
		for(KeyPair p : outstandingPairs)
		{
			if(p.getPublic().getEncoded().toString().equals(pub.getEncoded().toString()))
				alreadyExists = true;
		}
		if(!alreadyExists)	
			this.outstandingPairs.add(new KeyPair(pub,priv));
		updateWallet();
	}
	
	/**
	 * writes outstanding key pairs to encrypted file for 
	 * retreival on next run of program
	 * @throws FileNotFoundException
	 */
	public void updateWallet() throws FileNotFoundException
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

		fullNode.updateWallet(data);
	}
	
	
	/**
	 * Creates a new Key Pair and adds to wallet
	 * @return
	 * @throws FileNotFoundException
	 */
	public PublicKey generateNewKey() throws FileNotFoundException
	{
		KeyPairGenerator keyGen=null;
		try {
			keyGen = KeyPairGenerator.getInstance("EC");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyGen.initialize(256);
		KeyPair newpair = keyGen.generateKeyPair();
		PrivateKey privKey = newpair.getPrivate();
		PublicKey pubKey = newpair.getPublic();
		outstandingPairs.add(newpair);
		updateWallet();
		return newpair.getPublic();
		
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
		ArrayList<HashMap> inputs = new ArrayList<HashMap>();
		ArrayList<HashMap> outputs = new ArrayList<HashMap>();
		ArrayList<KeyPair>keysForInputs = new ArrayList<KeyPair>();
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		double lastAmt = 0;
		double totalSending = amt;
		
		for(int i = 0; i < outstandingPairs.size(); i++)
		{
			KeyPair key = outstandingPairs.get(i);
			double remaining = bc.getKeyBalance(pool, key.getPublic());
			if(i == 0 && totalSending <= remaining)
			{

				try {
					
					Transaction t = new Transaction(key, bc, pool,destAddr,totalSending );
					return t;
					
					//i = outstandingPairs.size();
				} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
						| UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				}

			}
			else if(totalSending <= remaining)
			{
				keysForInputs.add(key);
				lastAmt = totalSending;
				HashMap op = new HashMap();
				op.put("amount",remaining-lastAmt);
				op.put("address",Utility.publicKeyToAddress(key.getPublic()));
				outputs.add(op);
				break;
			}
			else
			{
				keysForInputs.add(key);
				totalSending-=remaining;
				HashMap op = new HashMap();
				op.put("amount",0.0);
				op.put("address",Utility.publicKeyToAddress(key.getPublic()));
				outputs.add(op);
				
				spentPairs.add(key);
				
			}
		}
		for(KeyPair key: spentPairs)
			outstandingPairs.remove(key);
		
		HashMap op1 = new HashMap();
		op1.put("address", destAddr);
		op1.put("amount",amt);
		outputs.add(op1);
		for(KeyPair key: keysForInputs)
		{	
			try {
				inputs.add(Transaction.generateInput(key, outputs, bc, pool)); 
				
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			
		}
		Transaction newTransaction = new Transaction(inputs,outputs,destAddr);
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
	
	public double getBalance()
	{
		return balance;
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
		balance = 0;
		for(KeyPair p : this.outstandingPairs)
		{
			PublicKey publicKey = p.getPublic();
			balance += bc.getKeyBalance(pool,publicKey);
		}
		return balance;
	}
}


