package Others;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**Interfaccia remota che indica le operazioni che un client può richiedere ad un server*/
public interface ClientManager extends Remote {
	public static final String REMOTE_OBJECT_NAME = "CLIENT_MANAGER";
	/**Permette agli utenti di registrare callback
	 * @param stub interfaccia passata dall'utente per la callback
	 * @param token dell'utente
	 * @param username nome dell'utente
	 * @return 0 se la registrazione ha avuto successo, -1 altrimenti (se il token non è valido)
	 * @throws RemoteException*/
	public int registerCallback(String username, String token, CallBackInterface stub) throws RemoteException;
	
	/**Permette a un utente di seguirne un altro
	 * @param username utente da seguire
	 * @return 0 se ha successo, -1 se il token non è valido,-2 se l'utente da seguire non esiste,-3 se non sono amici, -4 se è già follower
	 * @throws RemoteException */
	public int follow(String token,String follower,String toFollow) throws RemoteException;
}