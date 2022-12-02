import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
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
	private ArrayList<Input> inputs;
	private ArrayList<Output> outputs;
	private long timestamp;

	
	
	/**
	 * Creates a new Transaction given input and outputs
	 * @param ip input HashMap<String, String>
	 * @param op output ArrayList
	 */
	public Transaction(ArrayList<Input> ip, ArrayList<Output> op)
	{
		id = UUID.randomUUID().toString();
		Date d = new Date();
		timestamp = d.getTime();
		inputs = ip;
		outputs = op;	
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
	public Transaction(KeyPair key,double senderBalance, String recipient, double amount) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException 
	{
		if (amount > senderBalance)
		{
			throw new IllegalArgumentException("amount: " + amount + "exceeds balance.");
		}

		
		outputs = new ArrayList<Output>();
		Output op1 = new Output(Utility.publicKeyToAddress(key.getPublic()), senderBalance - amount);
		outputs.add(op1);
		
		Output op2 = new Output(recipient, amount);
		outputs.add(op2);


		id = UUID.randomUUID().toString();
		this.signTransaction(senderBalance,key);
	}	
	
	
	
	/**
	 * creates mining reward
	 * @param w  current user's wallet
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Transaction getMiningRewardTransaction(String publicKey) throws FileNotFoundException
	{
		//Generate inputs
		ArrayList<Input> ips = new ArrayList<Input>();
		ips.add(new Input("mining reward",Blockchain.MINING_REWARD,"mining reward"));
		
		//generate outputs
		ArrayList<Output> op = new ArrayList<Output>();
		Output op1 = new Output(publicKey,Blockchain.MINING_REWARD);
		op.add(op1);
		return new Transaction(ips,op);
	}
	
	
	

	
	

	
	
	/**
	 * signs this transaction
	 * @param sender sender's wallet
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public void signTransaction(double currentBalance, KeyPair key) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException
	{
		inputs = new ArrayList<Input>();
		Input input = new Input(Utility.publicKeyToAddress(key.getPublic()),currentBalance,Utility.sign(Utility.hash(outputs.toString()),key.getPrivate()));
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
	public static Input generateInput(KeyPair key, ArrayList<Output> outputs, double currentBalance) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException
	{
		Input input = new Input(Utility.publicKeyToAddress(key.getPublic()),currentBalance,Utility.sign(Utility.hash(outputs.toString()),key.getPrivate()));
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
	public boolean verifyTransaction(Blockchain bc,TransactionPool pool) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchProviderException
	{
		
			
		
		Input senderInput = this.getInputs().get(0);
		
		double chainBalance = bc.getLastOutputBalance(senderInput.getAddress());
		pool.sortByTimestamp();
		ArrayList<Transaction> pendingTransactions = pool.getTransactionsFor(senderInput.getAddress());
		
		if(pendingTransactions.size() > 0)
		{
			Transaction lastInPool = pendingTransactions.get(pendingTransactions.size()-1);
			ArrayList<Output> outputs = lastInPool.getOutputs();
			int count = 0;
			for(Output output: outputs)
			{
				
				if(output.getAddress().equals(senderInput.getAddress()))
				{
					count++;
				
					if (output.getAmount()!=senderInput.getAmount())
						return false;	
				}
				if(count>1)
					return false;
			}
		}
		else
		{
			if(chainBalance != senderInput.getAmount())
				return false;
		}
		
		
		
		
			
		double totalOutput = 0;
		
		for(Output output: outputs)
			totalOutput += output.getAmount();
		
		if(senderInput.getAmount() != totalOutput)
			{return false;}
//		Don't think we need to verify mining reward transaction since added by miner
//		if(senderInput.getAddress().equals("mining_reward") && 
//				sendInput.getAmount()==Blockchain.MINING_REWARD &&
//				inputs.size() == 1 && outputs.size()==1)	
//			return true;
		
		
				
				
				
		boolean signaturesVerified = true;
		for(Input input: inputs)
		{
			PublicKey key = Utility.retrievePublicKey((String)(input.getAddress()));
			if(!Utility.verifySignature(key, Utility.hash(this.outputs.toString()), input.getSignature()))
				{signaturesVerified = false;}
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
		for(int i = 0; i<outputs.size(); i++)
		{
			resp += "\n\t" + outputs.get(i);
		}
		return resp;
	}
	
	
	public String getID()
	{
		return id;
	}
	
	public ArrayList<Input> getInputs()
	{
		return inputs;
	}
	
	public ArrayList<Output> getOutputs()
	{
		return outputs;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	


}
