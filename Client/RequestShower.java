package Client;

/**Runnable usato semplicemente per la visualizzazione di richieste d'amicizia nell'interfaccia*/
public class RequestShower implements Runnable{
	ClientGui gui;/**Interfaccia dell'utente che ha ricevuto la richiesta*/
	String senderName;/**Mittente della richiesta*/
	
	/**Costruttore: assegna l'interfaccia e il nome di chi ha mandato la richiesta
	 * @param gui interfaccia
	 * @param name utente mittente della richiesta*/
	public  RequestShower(ClientGui gui,String name){
		this.gui=gui;
		this.senderName=name;
	}
	
	@Override
	public void run(){
		this.gui.addRequest(this.senderName);//semplicemente aggiungi nella JList delle richieste la richiesta
	}
}