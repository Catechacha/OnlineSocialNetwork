package Client;
import Others.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.LineBorder;

/**Interfaccia grafica dell'utente*/
public class ClientGui extends JFrame{
	private static final long serialVersionUID = 1L;
	
	/*titoli delle finestre di errore, warning e informazione*/
	static String WARNING="Online Social Network: Warning";
	static String ERROR="Online Social Network: Error";
	static String OK="Online Social Network: Ok";
	/**/
	
	private SocialClient client=null;/**Utente che si associa all'interfaccia al momento del login; quando fa logout, client torna null*/
	
	/*COMPONENTI CLIENT PANEL: alcuni devo dichiararli fuori per renderli visibili ad altre funzioni*/
	JList<String> requestText = new JList<String>();/** Componente di Java Swing nel quale saranno visualizzate le richieste di amicizia che arrivano*/
	public JTextArea showcase;/**Componente di Java Swing nel quale saranno visualizzati i messaggi di coloro che sono seguiti dall'utente quando arrivano*/
	
	/**Assegna un utente (SocialClient) all'interfaccia, e cambia quindi l'interfaccia visualizzando quella delle funzionalità offerte all'utente
	 * @param client utente da assegnare*/
	public void setClient(SocialClient client) {
		this.client = client;
		this.setContentPane(clientPanel());
	}

	/**Procedura che richiama la chiusura dei due thread, quello di attesa di richieste di amicizia e quello per le risposte di keepalive*/
	public void callStopThreads(){
		if(this.client!=null)
			this.client.stopThreads();
	}
	
	/**Costruttore dell'interfaccia*/
	public ClientGui(){
		this.setSize(600,650);
		this.setTitle("Online Social Network");
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.setContentPane(beginPanel());
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){//se il client viene chiuso senza fare logout
            	callStopThreads();//richiama la procedura per chiudere i due thread (quello delle richieste di amicizia e quello di keepalive)
            	System.exit(0);//e poi esci
            }
        });
	}
	
	/**Costruttore del pannello iniziale contenuto nell'interfaccia*/
	/*Ho usato i layout (tanto per provare ad utilizzarli), anche se forse non rendono troppo gradevole l'insieme*/
	private JPanel beginPanel() {
		JPanel beginPanel=new JPanel();
		beginPanel.setBorder(new LineBorder(new Color(28, 57, 157),3));
		beginPanel.setBackground(new Color(171,205,239));
		beginPanel.setLayout(new GridLayout(2, 1, 2,2));
		beginPanel.add(new JLabel ("OnlineSocialNetwork: SignUp",SwingConstants.CENTER));
		beginPanel.getLayout().minimumLayoutSize(beginPanel);
			JPanel nestedPanel = new JPanel();
			nestedPanel.setLayout(new GridLayout(3,2,20,20));
			nestedPanel.setBackground(new Color(171,205,239));
			nestedPanel.add(new JLabel("User",SwingConstants.CENTER));
			JTextField userText= new JTextField();
			nestedPanel.add(userText);
			nestedPanel.add(new JLabel("Password",SwingConstants.CENTER));
			JPasswordField passwordText= new JPasswordField();
			passwordText.setEchoChar('*');
			nestedPanel.add(passwordText);
			Button signUpButton=new Button("Sign Up");
			signUpButton.setBackground(new Color(204,204,255));
			signUpButton.addActionListener(e -> { //assegno un action listener al pulsante di registrazione e un altro a quello di login
				if(userText.getText().length()==0|passwordText.getPassword()==null|passwordText.getPassword().length==0){
					JOptionPane.showMessageDialog(beginPanel, "Please insert username and password",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE, null);
				}else{
					if(userText.getText().contains(" ")||userText.getText().length()>14){
						JOptionPane.showMessageDialog(beginPanel, "The username must not contain spaces and its max lenght is 14",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE, null);
					}else{
						SocialClient sClient= new SocialClient(userText.getText(),String.valueOf(passwordText.getPassword()));
						//NOTA: nella registrazione creo solo un social client e gli faccio fare la registrazione, ma non lo assegno come client dell'interfaccia
						int result = sClient.registration();
						if(result==-1)
							JOptionPane.showMessageDialog(beginPanel,"Please insert another username (existent username)",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
						else
							JOptionPane.showMessageDialog(beginPanel,"Welcome! You're now registered in OnlineSocialNetwork",ClientGui.OK, JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			nestedPanel.add(signUpButton);
			Button loginButton=new Button("Login");
			loginButton.setBackground(new Color(204,204,255));
			loginButton.addActionListener(e-> {
				if(userText.getText().length()==0|passwordText.getPassword()==null|passwordText.getPassword().length==0){
					JOptionPane.showMessageDialog(beginPanel,"Please insert username and password",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
				}else{
					SocialClient sClient= new SocialClient(userText.getText(),String.valueOf(passwordText.getPassword()),this);
					Packet pResult = sClient.login();
					String result=pResult.getSpecificData(0);
					if(result.equals("-1"))
						JOptionPane.showMessageDialog(beginPanel,"This user is online",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
					else{
						if(result.equals("-2"))
							JOptionPane.showMessageDialog(beginPanel,"Wrong password, please retry",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
						else{
							if(result.equals("-3"))
								JOptionPane.showMessageDialog(beginPanel,"Not existent username",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
							else{
								//In questo caso, essendo andato a buon fine il login, posso assegnare all'utente loggato l'interfaccia
								setClient(sClient);
								//Metto le richieste di amicizia che ha ricevuto l'utente appena loggato nella JList
								String[] x= new String[pResult.getData().size()-1];
								for(int i=1;i<pResult.getData().size();i++)
									x[i-1]=pResult.getSpecificData(i);
								this.requestText.setListData(x);
								this.client.requestMessages();//richiedo al server i messaggi pendenti che sono arrivati quando ero offline
								this.revalidate();
							}
						}
					}				
				}				
			});
			nestedPanel.add(loginButton);
		beginPanel.add(nestedPanel);
		return beginPanel;
	}
	
	/**Metodo che mi costruisce il secondo pannello, ovvero quello in cui un utente è loggato e può fare le azioni descritte dalla specifica*/
	//NOTA: non ho usato layout in questo panel
	public JPanel clientPanel() {
		JPanel clientPanel=new JPanel();
		clientPanel.setBorder(new LineBorder(new Color(30, 9, 94),3));
		clientPanel.setBackground(new Color(171,205,239));
		clientPanel.setLayout(null);
		
		JLabel welcome = new JLabel(" Welcome "+this.client.getUsername()+"!");
		welcome.setBounds(10,10,150,30);
		clientPanel.add(welcome);
		JButton logoutButton = new JButton("Logout");
		logoutButton.setBounds(475,10,100,30);
		logoutButton.addActionListener(e-> {
		/*in caso di logout, se questo è andato a buon fine, fermo i 2 thread, quello delle richieste di amicizia e quello di risposte keepalive*/
			int result=this.client.logout();
			if(result==0){
				this.client.stopThreads();
				String[] x=new String[0];
				this.requestText.setListData(x);
				this.showcase.setText("");
				this.client=null;
				this.setContentPane(beginPanel());
				this.revalidate();
			}
		});
		clientPanel.add(logoutButton);
		
		JLabel searchResultLabel = new JLabel("Search Result:");
		searchResultLabel.setBounds(215,85,100,20);
		clientPanel.add(searchResultLabel);
		JTextArea searchResultText = new JTextArea();
		searchResultText.setBounds(215,110,200,500);
		clientPanel.add(searchResultText);
		
		JTextField searchBar = new JTextField();
		searchBar.setBounds(10,45,200,30);
		clientPanel.add(searchBar);
		JButton searchButton = new JButton("Search");
		searchButton.setBounds(225, 45, 100, 30);
		searchButton.addActionListener(e -> {
			String name=searchBar.getText();
			if(name.length()!=0){
				ArrayList<String> a=this.client.searchUser(name);
				if(a==null){//token non valido, devo rifare il login
					logoutButton.doClick();
					JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.INFORMATION_MESSAGE);
				}else{
					searchResultText.setText("");
					if(a.size()==0)
						JOptionPane.showMessageDialog(clientPanel,"No result for this name",ClientGui.WARNING, JOptionPane.INFORMATION_MESSAGE);
					else{
						for(int i=0;i<a.size();i++)
							searchResultText.append(a.get(i)+"\n");
					}
				}
			}else
				JOptionPane.showMessageDialog(clientPanel,"Please insert something to search",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
		});
		clientPanel.add(searchButton);
		JButton requestFriendButton = new JButton("Friend request");
		requestFriendButton.setBounds(340, 45, 120, 30);
		requestFriendButton.addActionListener(e -> {
			String name=searchBar.getText();
			if(name.length()!=0){
				int result =this.client.requestFriendship(name);
				if(result==-1){//token non valido, devo rifare il login
					logoutButton.doClick();
					JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
				}else
					if(result==-2)//utente non esistente o non online o già amici o utente e mandante sono la stessa persona
						JOptionPane.showMessageDialog(clientPanel,"Impossible to send request",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
					else
						JOptionPane.showMessageDialog(clientPanel,"Request sent",ClientGui.OK, JOptionPane.INFORMATION_MESSAGE);
			}else
				JOptionPane.showMessageDialog(clientPanel,"Please insert a username",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
		});
		clientPanel.add(requestFriendButton);
		JButton followButton = new JButton("Follow");
		followButton.setBounds(475, 45, 100, 30);
		followButton.addActionListener(e -> {
			String toFollow=searchBar.getText();
			if(toFollow.length()!=0){
				int result =this.client.follow(toFollow);
				if(result==-1){//token non valido, devo rifare il login
					logoutButton.doClick();
					JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
				}else
					if(result==-2)
						JOptionPane.showMessageDialog(clientPanel,"This user not exists",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
					else
						if(result==-3)
							JOptionPane.showMessageDialog(clientPanel,"You and "+toFollow+" are not friends",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
						else
							if(result==-4)
								JOptionPane.showMessageDialog(clientPanel,"You're already a follower",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
							else
								JOptionPane.showMessageDialog(clientPanel,"Congratulations! You follow "+toFollow,ClientGui.OK, JOptionPane.INFORMATION_MESSAGE);
			}else
				JOptionPane.showMessageDialog(clientPanel,"Please insert a username",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
		});
		clientPanel.add(followButton);
		
		JLabel showcaseLabel = new JLabel("Your Showcase:");
		showcaseLabel.setBounds(10,85,200,20);
		clientPanel.add(showcaseLabel);
		this.showcase = new JTextArea();
		showcase.setBounds(10,110,200,455);
		clientPanel.add(showcase);
		JTextField publishText = new JTextField();
		publishText.setBounds(10, 570, 120, 30);
		clientPanel.add(publishText);
		JButton publishButton=new JButton("Publish");
		publishButton.setBounds(130,570,80,30);
		publishButton.addActionListener(e -> {
			String toPublish=publishText.getText();
			if(toPublish.length()!=0){
				int result =this.client.publish(toPublish);
				if(result==-1){//token non valido, devo rifare il login
					logoutButton.doClick();
					JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
				}else{
					showcase.append("Me: "+toPublish+"\n");
					publishText.setText("");
				}
			}else
				JOptionPane.showMessageDialog(clientPanel,"Please insert something to publish",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
		});
		clientPanel.add(publishButton);
		
		JLabel friendsLabel= new JLabel("My Friends:");
		friendsLabel.setBounds(420,85,100,20);
		clientPanel.add(friendsLabel);
		JTextArea friendsText = new JTextArea();
		friendsText.setBounds(420,110,160,200);
		clientPanel.add(friendsText);
		JButton showFriendsButton=new JButton("Show Friends");
		showFriendsButton.setBounds(440, 315, 120, 30);
		showFriendsButton.addActionListener(e -> {
			ArrayList<String> a=this.client.requestFriendList();
			if(a==null){//token non valido, devo rifare il login
				logoutButton.doClick();
				JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.INFORMATION_MESSAGE);
			}else{
				if(a.size()==0)//se non hai amici
					JOptionPane.showMessageDialog(clientPanel,"Sorry but you have no friends",ClientGui.WARNING, JOptionPane.INFORMATION_MESSAGE);
				else{
					friendsText.setText("");
					for(int i=0;i<a.size();i++)
						friendsText.append(a.get(i)+"\n");
				}
			}
		});
		clientPanel.add(showFriendsButton);
		JLabel requestLabel= new JLabel("Request:");
		requestLabel.setBounds(420,350,100,20);
		clientPanel.add(requestLabel);
		requestText.setBounds(420,375,160,200);//requestText dichiarata fuori per chiamarci l'addRequest dal Runnable che aspetta richieste di amicizia
		clientPanel.add(requestText);
		JButton confirmButton=new JButton("Confirm");
		confirmButton.setBounds(420, 580, 80, 30);
		confirmButton.addActionListener(e -> {
			String username=this.requestText.getSelectedValue();
			if(username==null)
				JOptionPane.showMessageDialog(clientPanel,"Error: please select a request",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
			else{
				int result = this.client.confirmRequest(username);
				if(result==-1){//token non valido, devo rifare il login
					logoutButton.doClick();
					JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
				}else{
					if(result==-2)//sono già amici
						JOptionPane.showMessageDialog(clientPanel,"You're already friends",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
					else
						if(result==-3)//la richiesta non esiste
							JOptionPane.showMessageDialog(clientPanel,"Sorry, it's impossible to confirm this request",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
						else
							JOptionPane.showMessageDialog(clientPanel,"Congratulations! You and "+username+" are now friends",ClientGui.OK, JOptionPane.INFORMATION_MESSAGE);
					removeRequest(username);
					}
				}
			});
		clientPanel.add(confirmButton);
		JButton deleteButton=new JButton("Delete");
		deleteButton.setBounds(500, 580, 80, 30);
		deleteButton.addActionListener(e -> {//per confermare richieste d'amicizia
			String username=this.requestText.getSelectedValue();
			if(username==null)
				JOptionPane.showMessageDialog(clientPanel,"Error: please select a request",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
			else{
				int result = this.client.deleteRequest(username);
				if(result==-1){//token non valido, devo rifare il login
					logoutButton.doClick();
					JOptionPane.showMessageDialog(clientPanel,"Token not valid: please login",ClientGui.ERROR, JOptionPane.ERROR_MESSAGE);
				}else{
					if(result==-2)//sono già amici
						JOptionPane.showMessageDialog(clientPanel,"You're already friends",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
					else
						if(result==-3)//la richiesta non esiste
							JOptionPane.showMessageDialog(clientPanel,"Sorry, it's impossible to delete this request",ClientGui.WARNING, JOptionPane.WARNING_MESSAGE);
						else
							JOptionPane.showMessageDialog(clientPanel,"Request deleted",ClientGui.OK, JOptionPane.INFORMATION_MESSAGE);
					removeRequest(username);
					}
				}
			});
		clientPanel.add(deleteButton);
		return clientPanel;
	}

	/*I due metodi seguenti sono usati per l'aggiornamento dinamico della JList: quando arrivano richieste di amicizia le visualizzo SUBITO*/
	/**Rimuove una richiesta di amicizia dalla JList
	 * @param username nome dell'utente che ha fatto la richiesta e che va rimosso */
	private void removeRequest(String username) {
		ListModel <String> l = this.requestText.getModel();
		String[] newModel = new String[l.getSize()-1];
		int j=0;
		for(int i=0;i<l.getSize();i++)
			if(!l.getElementAt(i).equals(username)){
				newModel[j]=l.getElementAt(i);
				j++;
			}
		this.requestText.setListData(newModel);
	}
	
	/**Aggiunge una richiesta di amicizia nella JList apposita
	 * @param senderName nome dell'utente che ha inviato la richiesta */
	public void addRequest(String senderName) {
		ListModel <String> l = this.requestText.getModel();
		String[] newModel = new String[l.getSize()+1];
		if(l.getSize()!=0)
			for(int i=0;i<l.getSize();i++)
				newModel[i]=l.getElementAt(i);
		newModel[newModel.length-1]=senderName;
		this.requestText.setListData(newModel);
	}
}