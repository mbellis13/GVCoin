import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * valid messages to be sent between peers
 * @author mellis
 *
 */
public class Message implements Serializable
{
	private static final long serialVersionUID = 405131190761313L;
	
	enum Type{
		REPLACE_CHAIN,//call replace_chain
		REQUEST_CHAIN,//request current chain
		ADD_TRANS,//call updateOrAdd
		CLEAR_TRANS,//call clear transaction
		DONE,
		//NEW_NEIGHBOR
	}
	
	private String id;
	private long timestamp;
	private ArrayList<Block> chain;
	private Transaction transaction;
	private Type type;
	private ArrayList<Transaction> transactions;
	

	
	public Message(Type t, ArrayList<Block> c, Transaction tr, ArrayList<Transaction> trans)
	{
		type = t;
		timestamp = new Date().getTime();
		chain = c;
		transaction = tr;
		transactions = trans;
		id = UUID.randomUUID().toString();
	}



	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}



	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}



	/**
	 * @return the chain
	 */
	public ArrayList<Block> getChain() {
		return chain;
	}



	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}



	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}



	/**
	 * @return the transactions
	 */
	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}
}
