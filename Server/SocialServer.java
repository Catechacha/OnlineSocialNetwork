package Server;
import Others.*;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

public class SocialServer {
	public static UserList u=null; /**Il server mantiene una lista di utenti*/
	
	/*from CONFIGURATION FILE*/
	public static int PORT;/** Porta su cui aspetta connessioni TCP */
	public static int BLOCK_SIZE;/** Buffer size */
	public static int TIMEFRIENDREQUEST;/**Tempo in millisecondi oltre il quale le richieste di amicizia non sono più valide (5 minuti)*/
	public static int RMIREGISTRYPORT;/**Porta per RMI registry*/
	public static int MULTICASTPORT;/**Porta per il multicast*/
	public static String MULTICASTADDRESS;/**Indirizzo per il multicast*/
	public static String REGISTRY_HOST;/** Porta per la creazione del registry RMI*/
	public static int REPLYPORT;/**Porta su cui il server riceverà le risposte di keepalive*/
	public static String PATHFILE;/**Path a cui verrà creato il file con la lista di utenti*/

	public static void main(String[] args){
		TakeConfiguration();
		StartUserList();//funzione che all'avvio del server controlla se c'è già un file da cui prendere la lista di utenti
		InitializationRMI();//inizializzazioni da fare per la parte RMI
		InitializationKeepAlive();//inizializzazioni da fare per la parte KEEPALIVE
		
		ExecutorService exe=null;
		Socket client = null;
		try(ServerSocket server=new ServerSocket()){
			server.bind(new InetSocketAddress(InetAddress.getLocalHost(),PORT));
			exe=Executors.newCachedThreadPool();
			while(true){
				try{
					client=server.accept();
					ClientHandler handler=new ClientHandler(client);
					exe.submit(handler);
				}catch(IOException e){
					System.err.println("Some error appeared");
				}
			}
		}catch(UnknownHostException e){
			System.err.println("Unknown Host");
		}catch(IOException e){
			System.err.println("Some error appeared");
		}finally{
			if (exe!=null)
				exe.shutdown();
			try{client.close();}catch(IOException e){System.err.println("Some error appeared");}
		}
	}

	/**Carica le variabili da file*/
	private static void TakeConfiguration() {
		try{
			BufferedReader bf = new BufferedReader(new FileReader("File/confServer.txt"));
			String line=bf.readLine();
			PORT=Integer.parseInt(line);
			BLOCK_SIZE=Integer.parseInt(bf.readLine()); 
			TIMEFRIENDREQUEST=Integer.parseInt(bf.readLine()); 
			RMIREGISTRYPORT=Integer.parseInt(bf.readLine());
			MULTICASTPORT=Integer.parseInt(bf.readLine()); 
			MULTICASTADDRESS=bf.readLine(); 
			REGISTRY_HOST=bf.readLine();
			REPLYPORT=Integer.parseInt(bf.readLine());
			PATHFILE=bf.readLine();
			bf.close();
		}catch(IOException e){
			System.err.println("An error occured taking initial configuration");
		}
	}

	/**Carica la userlist se già esiste in un file, altrimenti ne crea una nuova*/
	private static void StartUserList() {
        try {
    		File userListFile = new File((String)(PATHFILE));//file dove salvo la lista utenti
    		if(userListFile.exists()){//se il file esiste --> carico la lista utenti
    			ObjectInputStream input = new ObjectInputStream(new FileInputStream(userListFile));
                u=(UserList) input.readObject();
                input.close();
                u.resetUserList();
    		}else{//se il file non esiste faccio una lista nuova e creo il file
            	u= new UserList();
            	userListFile.createNewFile();
            	u.save();
    		}
        } catch (IOException | NullPointerException | ClassNotFoundException e) {
            System.err.println("User list creation error");
        }	
	}

	/**Fa partire il thread che manda messaggi in multicast per il keepalive*/
	private static void InitializationKeepAlive() {
		Thread t = new Thread(new KeepAliveServerWriter(MULTICASTPORT, MULTICASTADDRESS, REPLYPORT, u));
		t.start();
	}

	/**Inizializzazione della parte gestita con RMI: creazione, esportazione..*/
	private static void InitializationRMI() {
		try{
			ClientManager managerStub = (ClientManager) UnicastRemoteObject.exportObject( new ClientManagerImpl(), 0);//0=porta aleatoria
			Registry registry= LocateRegistry.createRegistry(RMIREGISTRYPORT);
			registry.rebind(ClientManager.REMOTE_OBJECT_NAME, managerStub);
		}catch(RemoteException e){
			System.err.println("Server configuration RMI failed");
		}
	}	
}