
public class Input 
{

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
	
	
	
	

}
