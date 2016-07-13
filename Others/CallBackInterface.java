package Others;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**Interfaccia remota per le callback*/
public interface CallBackInterface extends Remote {
	/**Permette al server di inviare il messaggio message ad un follower. Il metodo stampa direttamente il messaggio a video
	 * @param message messaggio da mandare
	 * @throws RemoteException */
	public void sendMessage(String message) throws RemoteException;
}
