import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;


/**
 * Individual Transaction
 * @author mellis
 *
 */
public class Transaction implements Serializable
{
	private static final long serialVersionUID = 134031190761313L;
	
	private String id;
	private ArrayList<HashMap> inputs;
	private String recipient;
	private ArrayList<HashMap> outputs;

	
	
	/**
	 * Creates a new Transaction given input and outputs
	 * @param ip input HashMap
	 * @param op output ArrayList
	 */
	public Transaction(ArrayList<HashMap> ip, ArrayList<HashMap> op, String r)
	{
		id = UUID.randomUUID().toString();
		Date d = new Date();
		inputs = ip;
		outputs = op;	
		recipient = r;
	}
	
	/**
	 * creates mining reward
	 * @param w  current user's wallet
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Transaction miningRewardTransaction(Wallet w) throws FileNotFoundException
	{
		HashMap ip = new HashMap();
		Date d = new Date();
		ip.put("amount", Blockchain.MINING_REWARD);
		ip.put("timestamp",d.getTime());
		ip.put("address","mining reward");
		ip.put("signature","mining reward");
		ArrayList<HashMap> ips = new ArrayList<HashMap>();
		ips.add(ip);
		
		PublicKey key = w.generateNewKey();
		String r = Utility.publicKeyToAddress(key);
		
		ArrayList<HashMap> op = new ArrayList<HashMap>();
		HashMap op1 = new HashMap();
		op1.put("amount", Blockchain.MINING_REWARD);
		op1.put("address",r);
		op.add(op1);
		return new Transaction(ips,op,r);
	}
	
	
	
	/**
	 * Create a Transaction given sender's wallet recipient
	 * 		address and amount to send
	 * @param sender  sender's wallet object
	 * @param recipient recipient's address
	 * @param amount number of coin to send
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeySpecException 
	 */
	public Transaction(KeyPair key,Blockchain bc, TransactionPool pool, String[] recipient, double amount) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException 
	{
		if (amount > bc.getKeyBalance(pool, key.getPublic()))
		{
			throw new IllegalArgumentException("amount: " + amount + "exceeds balance.");
		}
		this.recipient = recipient[0];
		double recipientBalance = bc.getKeyBalance(pool, Utility.retrievePublicKey(this.recipient));
		
		outputs = new ArrayList<HashMap>();
		HashMap op1 = new HashMap();
		op1.put("amount",0.0);// bc.getKeyBalance(pool,key.getPublic()) - amount);
		op1.put("address",Utility.publicKeyToAddress(key.getPublic()));
		outputs.add(op1);
		
		HashMap op2 = new HashMap();
		op2.put("amount",amount + recipientBalance);
		op2.put("address",recipient[0]);
		outputs.add(op2);
		
		if(recipient.length > 1) //index 0 for recipient, index 1 for change
		{
			HashMap op3 = new HashMap();
			op3.put("amount",bc.getKeyBalance(pool,key.getPublic()) - amount);
			op3.put("address",recipient[1]);
			outputs.add(op3);
		}
		
		
		id = UUID.randomUUID().toString();
		//input = new HashMap();
		Date d = new Date();
//		input.put("amount", sender.getBalance());
//		input.put("date",d.getTime());
//		input.put("address", )
//		input.put("signature",sender.sign(Utility.hash(outputs.toString())));
		this.signTransaction(bc,key,pool);
	}
	
	
	/**
	 * Update an existing Transaction with a new output
	 * @param sender sender's wallet object
	 * @param recipientAddress recipient's public address
	 * @param amount   number of coin to send
	 * @return the updated Transaction or null if no Transaction found
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public Transaction update(KeyPair key,Blockchain bc, TransactionPool pool, String recipientAddress, double amount) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException
	{
		HashMap senderOutput = null;
		//System.out.println("size: " + outputs.size());
		for(HashMap map : outputs)
		{
			if(map.get("address").equals(Utility.publicKeyToAddress(key.getPublic())))
			{
				senderOutput = map;
			}
		}

		
		if(amount > (Double)senderOutput.get("amount"))
		{
			System.out.println("Amount: " + amount+ " exceeds available coins");
		}
		else
		{
			senderOutput.replace("amount", (Double) (senderOutput.get("amount")) - amount);
			
			HashMap n = new HashMap();
			n.put("amount",amount);
			n.put("address", recipientAddress);
			this.outputs.add(n);
			this.signTransaction(bc,key,pool);
			
			return this;
		}
		return null;
		
	}
	
	
	/**
	 * signs this transaction
	 * @param sender sender's wallet
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public void signTransaction(Blockchain bc, KeyPair key, TransactionPool pool) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException
	{
		inputs = new ArrayList<HashMap>();
		HashMap input = new HashMap();
		Date d = new Date();
		input.put("amount", bc.getKeyBalance(pool, key.getPublic()));
		input.put("timestamp",d.getTime());
		input.put("address",Utility.publicKeyToAddress(key.getPublic()));
		input.put("signature",Utility.sign(Utility.hash(outputs.toString()),key.getPrivate()));
		inputs.add(input);
	}
	
	/**
	 * generates inputs for given transaction
	 * @param key
	 * @param outputs
	 * @param bc
	 * @param pool
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public static HashMap generateInput(KeyPair key, ArrayList<HashMap> outputs, Blockchain bc, TransactionPool pool) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException
	{
		HashMap input = new HashMap();
		Date d = new Date();
		input.put("amount", bc.getKeyBalance(pool, key.getPublic()));
		input.put("timestamp",d.getTime());
		input.put("address",Utility.publicKeyToAddress(key.getPublic()));
		input.put("signature",Utility.sign(Utility.hash(outputs.toString()),key.getPrivate()));
		return input;
	}
	
	
	/**
	 * Verify's that the Transaction t is valid and
	 * has a valid signature
	 * @param t Transaction to verify
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	public static boolean verifyTransaction(Transaction t, Blockchain bc,TransactionPool pool) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException
	{
		
			
		//System.out.println("TEST\n" + t);
		//System.out.println("one" + "\n"+t);
		double totalInput = bc.getKeyBalanceForVerification(pool, Utility.retrievePublicKey(t.getRecipient()), t);
		System.out.println("dest bal: " + totalInput);
		for(HashMap input: t.getInput())
		{
			PublicKey key = Utility.retrievePublicKey((String)(input.get("address")));
			if((Double)(input.get("amount"))!=bc.getKeyBalanceForVerification(pool, key, t))
				{
				System.out.println("input: " + input.get("amount"));
				System.out.println("balance: " + bc.getKeyBalanceForVerification(pool, key, t));
				System.out.println("ONE");
				return false;}
			totalInput += (Double)input.get("amount");
		}
			
		double totalOutput = 0;
		for(HashMap h: t.outputs)
			totalOutput += (Double)h.get("amount");
		
		if(totalInput != totalOutput)
			{System.out.println("TWO");return false;}
		if(t.getInput().get(0).get("address").equals("mining_reward") && 
				(Double)(t.getInput().get(0).get("address"))==Blockchain.MINING_REWARD &&
				t.getInput().size() == 1 && t.getOutputs().size()==1)	
			return true;
		
		
				
				
				
		boolean signaturesVerified = true;
		for(HashMap input:t.getInput())
		{
			PublicKey key = Utility.retrievePublicKey((String)(input.get("address")));
			if(!Utility.verifySignature(key, Utility.hash(t.outputs.toString()), (String)(input.get("signature"))))
				{System.out.println("three");signaturesVerified = false;}
		}
		return signaturesVerified;
		
		//Utility.verifySignature(key, Utility.hash(t.outputs.toString()), (String)(t.input.get("signature")));
	}
	
	

	
	/**
	 * returns string representation of the Transition
	 */
	public String toString()
	{
		String resp = "Transaction:\n" +
						"\n\tid     : " + id +
						"\n\tinput  : " + inputs +
						"\n\toutput : ";
		for(int i = 0; i < outputs.size(); i++)
		{
			resp += "\n\t" + outputs.get(i);
		}
		return resp;
	}
	
	
	
	
	public String getID()
	{
		return id;
	}
	
	public ArrayList<HashMap> getInput()
	{
		return inputs;
	}
	
	public ArrayList<HashMap> getOutputs()
	{
		return outputs;
	}
	
	public String getRecipient()
	{
		return recipient;
	}
	


}
