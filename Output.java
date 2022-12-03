import java.io.Serializable;

public class Output implements Serializable
{
	private static final long serialVersionUID = 98628282822051612L;
	private String address;
	private double amount;
	
	public Output(String address, double amount)
	{
		this.address = address;
		this.amount = amount;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public double getAmount()
	{
		return amount;
		
	}
	
	@Override
	public String toString()
	{
		return "\n\t\taddress: " + address + "\n\t\tamount: " + amount;
	}

}
