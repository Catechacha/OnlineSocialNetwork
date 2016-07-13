package Server;

import Others.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class User implements Serializable{
	private static final long serialVersionUID = 1L;//mi serve per il salvataggio
	
	/** Struttura dati per mantenere lo stato del singolo utente */
	private String name;/** Nome dell'utente*/
	private String password; /** Password dell'utente*/
	private String token; /** Token dell'utente*/
	private int requestPORT;/**Porta sulla quale l'utente attende richieste di amicizia*/ 
	private CopyOnWriteArrayList<String> friendList;/**Lista di amici dell'utente*/ 
	private CopyOnWriteArrayList<Request> requestList;/**Lista di richieste di amicizia dell'utente*/
	private CopyOnWriteArrayList<String> follower;/**Lista di follower dell'utente*/
	private CallBackInterface callback; /** Permette al server di contattare l'utente*/
	private CopyOnWriteArrayList<String> messages;/**Raccoglie gli aggiornamenti dei follower che arrivano all'utente quando questo è offline*/
	boolean alive;/**Mi dice se un utente ha risposto al messaggio di keepalive o meno*
	
	/** Crea un nuovo utente
	 * @param name Nome del nuovo utente
	 * @param password Password del nuovo utente */
	public User(String name,String password){
		this.name=name;
		this.password=password;
		this.token=null;
		this.friendList = new CopyOnWriteArrayList<String>();
		this.requestList = new CopyOnWriteArrayList<Request>();
		this.follower=new CopyOnWriteArrayList<String>();
		this.messages=new CopyOnWriteArrayList<String>();
		this.alive=false;
	}
	
	/** Ritorna il nome dell'utente
	 * @return Nome dell'utente*/
	public String getName() {
		return name;
	}

	/** Ritorna la password dell'utente
	 * @return Password dell'utente*/
	public String getPassword() {
		return password;
	}

	/** Ritorna il token dell'utente
	 * @return Il token dell'utente */
	public String getToken() {
		return token;
	}

	/** Setta il token dell'utente
	 * @param token Nuovo token da assegnare all'utente */
	public void setToken(String token) {
		this.token = token;
	}

	/**Ritorna la lista degli amici di un utente
	 * @return la lista degli amici*/
	public CopyOnWriteArrayList<String> getFriendlist() {
		return this.friendList;
	}

	/** Ritorna la porta sulla quale il client attende richieste di amicizia
	 *  @return la porta suddetta*/
	public int getRequestPORT() {
		return requestPORT;
	}

	/** Setta la porta sulla quale il client attende le richieste
	 * @param requestPORT intero che rappresenta la porta suddetta da settare */
	public void setRequestPORT(int requestPORT) {
		this.requestPORT = requestPORT;
	}
	
	/** Aggiunge un elemento alla lista di richieste di amicizia
	 * @param name stringa contenente il nome utente da aggiungere alla lista*/
	public void addRequest(String name) {
		this.requestList.add(new Request(name));
	}

	/**Ritorna la lista degli utenti che hanno mandato richieste di amicizia all'utente (SOLO NOME UTENTE)
	 * @return la lista suddetta*/
	public ArrayList<String> getRequestListOnlyUser() {
		ArrayList<String> a= new ArrayList<String>();
		for(Request r:this.requestList)
			a.add(r.getUser());
		return a;
	}
	
	/**Ritorna la lista di richieste ricevute dall'utente
	 * @return la lista suddetta*/
	public CopyOnWriteArrayList<Request> getRequestList() {
		return this.requestList;
	}

	/** Aggiunge un amico alla lista degli amici dell'utente
	 * @param x amico da aggiungere  */
	public void addFriend(String x) {
		this.friendList.add(x);
	}

	/** Metodo che rimuove la richiesta d'amicizia di senderName dalla lista
	 * @param senderName nome dell'utente che ha fatto la richiesta da cancellare */
	public void removeRequest(String senderName){
		boolean found=false;
		int i=0;
		while(found==false){
			Request r=this.getRequestList().get(i);
			if(r.getUser().equals(senderName)){
				this.requestList.remove(r);
				found=true;
			}
			i++;
		}
	}
	
	/**Metodo che ritorna la lista dei follower di un utente
	 * @return la lista dei follower*/
	public CopyOnWriteArrayList<String> getFollower() {
		return this.follower;
	}
	
	/**Metodo che permette di aggiungere un follower alla lista*/
	public void addFollower(String follower) {
		this.follower.add(follower);
	}
	
	/**Ritorna l'interfaccia per la callback dell'utente
	 * @return l'interfaccia suddetta */
	public CallBackInterface getCallback() {
		return callback;
	}
	
	/**	Permette di assegnare l'interfaccia per la callback
	 * @param stub interfaccia da assegnare*/
	public void setCallback(CallBackInterface stub) {
		this.callback=stub;
	}

	/**Aggiunge un messaggio da visualizzare per l'utente
	 * @param message messaggio da aggiungere alla lista dei messaggi di un utente */
	public void addMessage(String message) {
		this.messages.add(message);
	}

	/**Metodo che ritorna la lista dei messaggi non visualizzati di un utente
	 * @return la lista dei messaggi*/
	public CopyOnWriteArrayList<String> getMessages() {
		return this.messages;
	}

	/**Metodo che realizza il logout dell'utente: setta il token a null, la callback a null e la requestPort a 0*/
	public void resetUser() {
		token=null;
		callback=null;
		requestPORT=0;
		alive=false;
	}

	/** Cancella i messaggi dalla lista dei messaggi quando l'utente era offline*/
	public void resetMessages() {
		this.messages.clear();		
	}

	/**Setta il flag alive
	 * @param value valore a cui settare il flag*/
	public synchronized void setAlive(boolean value) {
		this.alive=value;
	}

	/**Ritorna il flag alive
	 * @return flag alive */
	public boolean getAlive() {
		return this.alive;
	}
}