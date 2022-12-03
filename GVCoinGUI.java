import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;

/**
 * Single entry point
 * Creates Wallet, Blockchain, Transaction Pool, and Server
 * @author mellis
 *
 */

public class GVCoinGUI extends JFrame implements MiningObserver
{
	
	
	private Blockchain chain;
	private Wallet wallet;
	private TransactionPool pool;
	private Miner miner;
	private P2pServer p2p;
	private ArrayList<String> neighbors;
	private ArrayList<Transaction> pending;
	private PrintWriter writer;
	
	private JLabel lbl_balance;
	private JLabel lbl_output;
	private boolean mining = false;
	private AESencryption encrypter;
	private String username;
	
	
	/**
	 * 
	 * Constructs the Full Node
	 * 
	 * @param keys existing key pairs
	 * @param block existing block chain
	 * @param uname username
	 * @param en AES encrypter 
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 */
	public GVCoinGUI(ArrayList<KeyPair> keys,ArrayList<KeyPair> spent,
			Blockchain block,String uname,AESencryption en) throws NoSuchAlgorithmException, FileNotFoundException
	{
		encrypter = en;
		username = uname;
		pending = new ArrayList<Transaction>();
		List<Message> incomingQueue = Collections.synchronizedList(new ArrayList<Message>());
		List<Message> outgoingQueue = Collections.synchronizedList(new ArrayList<Message>());
		pool = new TransactionPool();
		p2p = new P2pServer(incomingQueue, outgoingQueue);

		wallet = new Wallet(keys,spent);

		chain = block;

		miner = new Miner();

		
		
		
		
		
		
		
		
		this.setLayout(new GridBagLayout());
		this.setBounds(200,200,800,500);
		this.setTitle("GVCoin Full Node");
		GridBagConstraints gbc = new GridBagConstraints();
		
		
		JLabel lbl_bal = new JLabel("Balance:");
		lbl_bal.setPreferredSize(new Dimension(100,40));
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(lbl_bal,gbc);
		
		lbl_balance = new JLabel("GVC"+wallet.calculateBalance(chain,pool));
		lbl_balance.setPreferredSize(new Dimension(100,40));
		gbc.gridx++;
		add(lbl_balance,gbc);
		
		JPanel pnl_trans = new JPanel();
		pnl_trans.setLayout(new GridBagLayout());
		GridBagConstraints pnl_gbc = new GridBagConstraints();
		pnl_gbc.gridx=0;
		pnl_gbc.gridy = 0;
		Border border = BorderFactory.createTitledBorder("Create Transaction");
		pnl_trans.setBorder(border);
		pnl_trans.setPreferredSize(new Dimension(630,100));
		
		pnl_gbc.anchor = pnl_gbc.WEST;
		JLabel lbl_to = new JLabel("Recipient:");
		pnl_trans.add(lbl_to,pnl_gbc);
		
		pnl_gbc.gridy++;

		JLabel lbl_amt = new JLabel("Amount(GVC):");
		pnl_trans.add(lbl_amt,pnl_gbc);
		
		pnl_gbc.gridx = 1;
		pnl_gbc.gridy = 0;
		pnl_gbc.gridwidth = 4;
		JTextField txt_to = new JTextField();
		txt_to.setPreferredSize(new Dimension(520,25));
		pnl_trans.add(txt_to,pnl_gbc);
		
		pnl_gbc.gridx = 1;
		pnl_gbc.gridy = 1;
		pnl_gbc.gridwidth = 1;
		JTextField txt_amt = new JTextField();
		txt_amt.setPreferredSize(new Dimension(150,25));
		pnl_trans.add(txt_amt,pnl_gbc);
		
		gbc.gridx=2;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor=gbc.WEST;
		lbl_output = new JLabel("Test");
		add(lbl_output,gbc);
		
		JButton btn_trans = new JButton("Submit");
		btn_trans.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Transaction t = null;
				try {
					t = wallet.generateTransactions(txt_to.getText(), chain, pool, Double.parseDouble(txt_amt.getText()));
				} catch (NumberFormatException | InvalidKeySpecException | NoSuchProviderException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				pool.updateOrAddTransaction(t);	
				synchronized(outgoingQueue)
				{
					outgoingQueue.add(new Message(Message.Type.ADD_TRANS,null,t,null));
				}
				outgoingQueue.notifyAll();
				
				
				txt_to.setText("");
				txt_amt.setText("");
				lbl_balance.setText("GVC"+wallet.calculateBalance(chain,pool));
			}
			
		});
		pnl_gbc.gridx = 4;
		pnl_gbc.gridy = 1;
		pnl_gbc.anchor = pnl_gbc.EAST;
		btn_trans.setPreferredSize(new Dimension(80,40));
		pnl_trans.add(btn_trans,pnl_gbc);
		
		
		gbc.gridy=1;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		add(pnl_trans,gbc);
		MiningObserver observer = this;
		
		JButton btn_mine = new JButton("Start Mining");
		btn_mine.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!mining)
				{
					btn_mine.setText("Stop Mining");
					mining = true;
				}
				else
				{
					btn_mine.setText("Start Mining");
					mining = false;
				}
				repaint();
				
				new Thread(new MineThread(chain, pool, miner,wallet,observer)).start();
				
			}
			
		});
		gbc.gridy++;
		gbc.gridwidth = 1;
		add(btn_mine,gbc);
		
		JButton reviewBC = new JButton("Write Current Chain");
		gbc.gridy++;
		add(reviewBC,gbc);
		reviewBC.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PrintWriter out = null;
				//File outFile = null;
				try {
					//outFile = new File("CurrentChain.txt");
					out = new PrintWriter("CurrentChain.txt","UTF-8");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				System.out.println(chain.getChain().toString());
				out.println(chain.getChain().toString());
				out.close();

						
				
			}
			
		});
		
		
		gbc.gridx++;
		JButton receiveFunds = new JButton("Receive Funds");
		add(receiveFunds,gbc);
		
		gbc.gridy++;
		gbc.gridx--;
		JLabel addr = new JLabel("Receive Address:");
		add(addr,gbc);
		
		gbc.gridy++;
		gbc.gridwidth = 6;
		JTextField receiveAddr = new JTextField();
		receiveAddr.setPreferredSize(new Dimension(600,40));
		receiveAddr.setEditable(false);
		add(receiveAddr,gbc);
		
		receiveFunds.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					receiveAddr.setText(Utility.publicKeyToAddress(wallet.generateNewKey().getPublic()));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		
		});
		synchronized(outgoingQueue)
		{
			outgoingQueue.add(new Message(Message.Type.REQUEST_CHAIN,null,null,null));
		}
		
		lbl_balance.setText("GVC"+wallet.calculateBalance(chain,pool));
		this.setVisible(true);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
	}
	
	
	public boolean getMining()
	{
		return mining;
	}

	
	public void update()
	{
		lbl_balance.setText("GVC"+wallet.calculateBalance(chain,pool));
		repaint();
	}
	
	public void updateWallet(String keys) throws FileNotFoundException
	{
		File outFile = new File(username + ".txt");
		PrintWriter writer = new PrintWriter(outFile);
		writer.println(encrypter.encrypt(username));
		writer.print(encrypter.encrypt(keys));
		writer.close();
	}
	
	
	@Override
	public void blockFound(Block block) 
	{
		chain.addBlock(block);
		this.update();
		
	}
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, InvalidKeySpecException, NoSuchProviderException
	{
		ArrayList<KeyPair> keys = new ArrayList<KeyPair>();
		ArrayList<KeyPair> spent = new ArrayList<KeyPair>();
		String name = JOptionPane.showInputDialog("Enter Username");
		JPasswordField pf = new JPasswordField();
		int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		String password = null;
		if (okCxl == JOptionPane.OK_OPTION) 
		  password = new String(pf.getPassword());

		File nameFile = new File(name+".txt");

		AESencryption encrypter = new AESencryption(password);
		if(nameFile.exists())
		{
			System.out.print("exists");
			Scanner read = new Scanner(nameFile);
			String line = read.nextLine();
			if(name.equals(encrypter.decrypt(line)))
			{
				System.out.println("correct");
				String strkeys = "";
				int j = 0;
				while(read.hasNextLine())
				{
					strkeys += read.nextLine() + "\n";
				}
				strkeys = encrypter.decrypt(strkeys.trim());
				String[] keysStr = strkeys.split("\n");
				for(int i = 0; i<keysStr.length; i+=3)
				{
					String type = keysStr[i];
					PrivateKey privKey = Utility.retrievePrivateKey(keysStr[i+1]);
					PublicKey pubKey = Utility.retrievePublicKey(keysStr[i+2]);
					
					KeyPair key = new KeyPair(pubKey,privKey);
					
					if(type.equals("os key")) 
						keys.add(key);
					else if(type.equals("spent key"))
						spent.add(key);
					System.out.println(j + " keys added");
					
				}

			}
			else
			{
				System.out.println("incorrect login");
				System.exit(1);
			}
				
		}
			
		else
		{

			System.out.println("new wallet created");
		}
		
		Blockchain block = new Blockchain();
		ArrayList<Block> blocks = new ArrayList<Block>();
		File findblock = new File("blockchain.dat");
		if(findblock.exists())
		{
			try {
				FileInputStream fileIn = new FileInputStream("blockchain.dat");
				ObjectInputStream objectIn = new ObjectInputStream(fileIn);
				
				block = (Blockchain) objectIn.readObject();
				

				System.out.println("*********\n"+block.getChain());
				objectIn.close();
				
				
			}catch (Exception ex) {
				System.out.println("error reading chain");
				System.exit(1);
			}
			
		}
		else
		{
			System.out.println("no existing blockchain found");
		}
		
		new GVCoinGUI(keys,spent, block,name,encrypter);
	}


	


	
}



/**
 * Thread for mining
 * @author mellis
 *
 */
class MineThread implements Runnable
{
	private boolean mining;
	private MiningObserver observer;
	private Miner miner;
	private Wallet wallet;
	private Blockchain blockchain;
	private TransactionPool pool;
	
	public MineThread(Blockchain blockchain, TransactionPool pool, Miner m,Wallet w, MiningObserver observer)
	{
		mining = true;
		this.pool = pool;
		this.blockchain = blockchain;
		this.miner = m;
		this.wallet = w;
		this.observer = observer;
	}
	
	public void setMining(boolean mining)
	{
		this.mining = mining;
	}
	
	@Override
	public void run() {
		KeyPair key = null;
		do {
			if(key==null)
			{
				try {
					key = wallet.generateNewKey();
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
			}
			
			try {

				Block b = miner.mineBlock(blockchain, key, pool.getPool());
				if(b != null)
				{
					blockchain.addBlock(b);
					observer.blockFound(b);
					key = null;
				}
				
		
				
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| UnsupportedEncodingException | InvalidKeySpecException | NoSuchProviderException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		while(mining);
	}
}
