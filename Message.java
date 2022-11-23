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
	
	enum Message_Type{
		REPLACE_CHAIN,//call replace_chain
		REQUEST_CHAIN,//request current chain
		ADD_TRANS,//call updateOrAdd
		CLEAR_TRANS,//call clear transaction
		DONE,
		//NEW_NEIGHBOR
	}
	
	public String id;
	public long timestamp;
	public ArrayList<Block> chain;
	public Transaction transaction;
	public Message_Type type;
	public ArrayList<Transaction> transactions;
	

	
	public Message(Message_Type t, ArrayList<Block> c, Transaction tr, ArrayList<Transaction> trans)
	{
		type = t;
		timestamp = new Date().getTime();
		chain = c;
		transaction = tr;
		transactions = trans;
		id = UUID.randomUUID().toString();
	}
}
