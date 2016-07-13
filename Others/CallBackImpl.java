package Others;
import Client.ClientGui;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**Implementazione dell'interfaccia remota per le callback*/
public class CallBackImpl extends RemoteObject implements CallBackInterface{
	private static final long serialVersionUID = 1432461361478933901L;
	ClientGui gui;
	
	/**Gli passo l'interfaccia grafica dell'utente collegato perchè in questo modo stampa subito messaggi a video*/
	public CallBackImpl(ClientGui gui) {
		this.gui=gui;
	}

	@Override
	public void sendMessage(String message) throws RemoteException{
			this.gui.showcase.append(message);//se l'utente è online visualizzo il messaggio direttamente nel suo swhocase
	}
}
