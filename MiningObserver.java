
public interface MiningObserver 
{
	void blockFound(Block block);
	void transactionReceived();
}
