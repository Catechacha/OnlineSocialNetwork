package Client;
import Others.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class SocialClient implements SocialClientInterface {
	//from CONF FILE
	public static int TCPPORT;/**Per mandare richieste su TCP/*/
	public static int MULTICASTPORT;/**Porta per il multicast*/
	public static String MULTICASTADDRESS;/**Indirizzo per il multicast*/
	public static String HOSTTOREPLY;/**Host a cui inviare i messaggi di risposta keepalive*/
	public static int REPLYPORT;/**Porta a cui inviare i messaggi di risposta keepalive*/
	public static int RMIREGISTRYPORT;/**Porta per RMI registry*/
	public static String REGISTRY_HOST;/** Porta per la creazione del registry RMI*/
	
	private String username;/**Username dell'utente*/
	private String password;/** Password dell'utente*/
	private String token;/** Token assegnato ad un utente al momento del login*/
	private long timeToken;/** Momento in millisecondi in cui è arrivato il token*/
	private int requestPort;/**Porta sulla quale un utente aspetta richieste di amicizia*/
	public ClientGui gui; /**Interfaccia grafica sulla quale l'utente sta lavorando*/
	ClientManager manager; /**per RMI:*/
	CallBackInterface stub; /** per RMI*/
	
	
	/**Costruttore del socialClient (usato per la procedura di registrazione nuovo utente)
	 * @param uname username dell'utente
	 * @param pass password dell'utente */
	public SocialClient(String uname, String pass) {
		TakeConfiguration();//leggi alcune variabili dal file di configurazione
		username=uname;
		password=pass;
	}
	
	/**Carica le variabili da file*/
	private void TakeConfiguration() {
		try{
			BufferedReader bf = new BufferedReader(new FileReader("File/confClient.txt"));
			TCPPORT=Integer.parseInt(bf.readLine());
			MULTICASTPORT=Integer.parseInt(bf.readLine());
			MULTICASTADDRESS=bf.readLine(); 
			HOSTTOREPLY=bf.readLine();
			REPLYPORT=Integer.parseInt(bf.readLine());
			RMIREGISTRYPORT=Integer.parseInt(bf.readLine());
			REGISTRY_HOST=bf.readLine();
			bf.close();
		}catch(IOException e){
			System.err.println("An error occured taking initial configuration");
		}
	}

	/**Costruttore del socialClient (usato per la procedura di login e altre)
	 * @param uname username dell'utente
	 * @param pass password dell'utente 
	 * @param clientGui interfaccia grafica che l'utente sta attualmente usando*/
	public SocialClient(String uname, String pass, ClientGui clientGui) {
		TakeConfiguration();//leggi alcune variabili dal file di configurazione
		username=uname;
		password=pass;
		gui=clientGui;
		//inizializzazione parte RMI: cerco il registro e ottengo un riferimento. Inoltre esporto l'oggetto per le callback
		try {
			this.stub=(CallBackInterface) UnicastRemoteObject.exportObject(new CallBackImpl(this.gui),0);
			this.manager = (ClientManager) LocateRegistry.getRegistry(REGISTRY_HOST,RMIREGISTRYPORT).lookup(ClientManager.REMOTE_OBJECT_NAME);
		} catch (RemoteException | NotBoundException e) {
			System.err.println("An error appeared working with RMI");
			e.printStackTrace();
		}
	}
	
	/** Metodo che ritorna l'username dell'utente
	 * @return l'username */
	public String getUsername(){
		return this.username;
	}
	
	@Override
	public int registration(){
		Packet result=null;
		try {
			SocketStructure s=new SocketStructure(TCPPORT);
			Packet p =new Packet(Packet.Type.REGISTRATION);
			p.addData(this.username);
			p.addData(this.password);
			s.send(p);
			result=(Packet) s.receive();
			s.closeStructure();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("An error appeared working with socket");
		}
		return Integer.parseInt(result.getSpecificData(0));
	}

	@Override
	public Packet login() {
		Packet result = null;
		try {	
			//apro una serversocket su cui aspetterò richieste di amicizia
			ServerSocket ss= new ServerSocket(0);
	        FriendRequestRunnable r = new FriendRequestRunnable(ss,this.gui);//gli passo la gui perchè poi dovrò visualizzare le richieste a video
			
			SocketStructure s=new SocketStructure(TCPPORT);
			Packet p =new Packet(Packet.Type.LOGIN);
			p.addData(this.username);
			p.addData(this.password);
			p.addData(ss.getLocalPort());//gli mando anche la porta su cui ho aperto ServerSocket
			s.send(p);
			result=(Packet) s.receive();
			s.closeStructure();
			//Se il login è avvenuto con successo, il client registra una callback con la quale il server lo può contattare
			if(!result.getSpecificData(0).equals("-1")&&!result.getSpecificData(0).equals("-2")&&!result.getSpecificData(0).equals("-3")){
				setToken(result.getSpecificData(0));
				//creo un runnable che deve aspettare le richieste di amicizia sulla porta che avevo comunicato al server
				Thread t=new Thread(r);
				t.start();
				//creo un runnable per rispondere ai messaggi di keepAlive
				Thread tt= new Thread(new KeepAliveClient(MULTICASTPORT, MULTICASTADDRESS, username, HOSTTOREPLY, REPLYPORT));
				tt.start();
				//registro la callback
				try{
					manager.registerCallback(this.getUsername(),this.token, this.stub);
				}catch(RemoteException e){
					System.err.println("An error appeared working with RMI");
					e.printStackTrace();
				}
				this.requestPort=ss.getLocalPort();//salvo la porta su cui aspetto richieste di amicizia!
			}else
				ss.close();
		} catch (IOException  | ClassNotFoundException e) {
			System.err.println("An error appeared working with socket");
		}
		return result;
	}
	
	@Override
	public int logout(){
		Packet p= null;
		try {
			SocketStructure s=new SocketStructure(TCPPORT);			
			p =new Packet(Packet.Type.LOGOUT,this.token);
			p.addData(this.username);
			s.send(p);
			s.closeStructure();
		} catch (IOException e) {
			System.err.println("An error appeared working with socket");
		}
		return 0;
	}

	@Override
	public int requestFriendship(String name) {
		Packet result= null;
		if(this.controlToken()){
			try {
				SocketStructure s=new SocketStructure(TCPPORT);			
				Packet p =new Packet(Packet.Type.FRIENDREQUEST,this.token);
				p.addData(this.username);
				p.addData(name);
				s.send(p);
				result=(Packet) s.receive();
				s.closeStructure();
				if(result.getType().equals(Packet.Type.TOKENERROR))
					return -1;
			} catch (IOException  | ClassNotFoundException e) {
				System.err.println("An error appeared working with socket");
			}
		}else
			return -1;
		return Integer.parseInt(result.getSpecificData(0));
	}

	@Override
	public int confirmRequest(String senderName) {
		Packet result= null;
		if(this.controlToken()){
			try {
				SocketStructure s=new SocketStructure(TCPPORT);			
				Packet p =new Packet(Packet.Type.CONFIRMREQUEST,this.token);
				p.addData(this.username);
				p.addData(senderName);
				s.send(p);
				result=(Packet) s.receive();
				s.closeStructure();
				if(result.getType().equals(Packet.Type.TOKENERROR))
					return -1;
			} catch (IOException  | ClassNotFoundException e) {
				System.err.println("An error appeared working with socket");
			}
		}else
			return -1;
		return Integer.parseInt(result.getSpecificData(0));
	}
	
	@Override
	public int deleteRequest(String senderName) {
		Packet result= null;
		if(this.controlToken()){
			try {
				SocketStructure s=new SocketStructure(TCPPORT);			
				Packet p =new Packet(Packet.Type.DELETEREQUEST,this.token);
				p.addData(this.username);
				p.addData(senderName);
				s.send(p);
				result=(Packet) s.receive();
				s.closeStructure();
				if(result.getType().equals(Packet.Type.TOKENERROR))
					return -1;
			} catch (IOException  | ClassNotFoundException e) {
				System.err.println("An error appeared working with socket");
			}
		}else
			return -1;
		return Integer.parseInt(result.getSpecificData(0));	
	}

	@Override
	public ArrayList<String> requestFriendList() {
		if(this.controlToken()){
			Packet p,response= null;
			try {
				SocketStructure s=new SocketStructure(TCPPORT);
				p =new Packet(Packet.Type.FRIENDLIST,this.token);
				p.addData(this.getUsername());
				s.send(p);
				response=(Packet) s.receive();
				s.closeStructure();
				if(response.getType().equals(Packet.Type.TOKENERROR))
					return null;
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("An error appeared working with socket");
			}
			return response.getData();
		}else
			return null;
	}

	@Override
	public ArrayList<String> searchUser(String name) {
		if(this.controlToken()){
			Packet p,response= null;
			try {
				SocketStructure s=new SocketStructure(TCPPORT);
				p =new Packet(Packet.Type.SEARCH,this.token);
				p.addData(name);
				s.send(p);
				response=(Packet) s.receive();
				s.closeStructure();
				if(response.getType().equals(Packet.Type.TOKENERROR))
					return null;
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("An error appeared working with socket");
			}
			return response.getData();
		}else
			return null;
	}

	@Override
	public int publish(String content) {
		Packet result= null;
		if(this.controlToken()){
			try {
				SocketStructure s=new SocketStructure(TCPPORT);			
				Packet p =new Packet(Packet.Type.PUBLISH,this.token);
				p.addData(this.username);
				p.addData(content);
				s.send(p);
				result=(Packet) s.receive();
				s.closeStructure();
				if(result.getType().equals(Packet.Type.TOKENERROR))
					return -1;
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("An error appeared working with socket");
			}
		}else
			return -1;
		return Integer.parseInt(result.getSpecificData(0));	
	}

	@Override
	public int follow(String name) {
		if(this.controlToken()){
			int result = 0;
			try {
				result = this.manager.follow(this.token,this.username,name);
			}catch (RemoteException e) {
				System.err.println("Comunication error with RMI: Impossible to follow");
				e.printStackTrace();
				result=-1;
			}
			return result;
		}else
			return -1;
	}
	
	@Override
	public boolean controlToken() {
		if((System.currentTimeMillis()-this.timeToken)<86400000)//86400000 sono 24 ore in millisecondi
			return true;
		return false;
	}
	
	/**Setta il token e il momento in cui si è memorizzato
	 * @param t token da settare nel client */
	public void setToken(String t){
		this.token=t;
		this.timeToken=System.currentTimeMillis();
	}

	/**Permette ad un utente di richiedere i messaggi che ha ricevuto quando era offline--> il server glieli invia tramite RMI*/
	//NOTA: qui non controllo il token, perchè questa funzione è praticamente inclusa nel login, in quanto la richiamo sempre dall'interfaccia appena fatto il login (Se andato a buon fine)
	public void requestMessages() {
		try {
			SocketStructure s=new SocketStructure(TCPPORT);			
			Packet p=new Packet(Packet.Type.REQUESTMESSAGES);
			p.addData(this.username);
			s.send(p);
			s.closeStructure();
		} catch (IOException e) {
			System.err.println("An error appeared working with socket");
		}	
	}

	/**Metodo che permette ad un client di far terminare il thread per le richieste di amicizia e quello di keepalive
	 * Come? Manda due pacchetti di terminazione*/
	public void stopThreads() {
		DatagramSocket ds = null;
		SocketStructure s = null;
		try{
			//terminazione thread richieste di amicizia: gli invio un pacchetto di tipo STOP
			s=new SocketStructure(this.requestPort);			
			Packet p=new Packet(Packet.Type.STOP);
			s.send(p);
			
			//terminazione del thread per risposte di keepalive: gli invio un pacchetto contenente l'username
			ds=new DatagramSocket();
			InetAddress address =InetAddress.getByName(MULTICASTADDRESS);
			DatagramPacket dp=new DatagramPacket((this.username).getBytes(),(this.username).getBytes().length,address,MULTICASTPORT);
			ds.send(dp);
		}catch(IOException e){
			System.err.println("An error occured closing threads");
		}finally{
			ds.close();
			try {s.closeStructure();} catch (IOException e) {System.err.println("An error occured closing socketStructure");}
		}
	}
}