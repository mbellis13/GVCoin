import java.io.Serializable;

public class Input implements Serializable
{
	private static final long serialVersionUID = 7654321131313131388L;

	private String signature;
	private double amount;
	private String address;
	
	public Input(String address, double amount, String signature)
	{

		this.signature=signature;
		this.amount=amount;
		this.address = address;
	}




	/**
	 * @return the signature
	 */
	public String getSignature() {
		return signature;
	}



	/**
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}



	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	
	@Override
	public String toString()
	{
		return "\n\t\tamount: " + amount+ "\n\t\taddress: " + address + "\n\t\tsignature: " + signature;
	}
	

}
