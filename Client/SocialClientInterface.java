package Client;
import java.util.ArrayList;
import Others.Packet;

/**Operazioni permesse da SimpleSocial agli utenti*/
public interface SocialClientInterface {
	
	/** Permette ad un client di registrarsi
	 * @return 0 se la registrazione ha successo, -1 altrimenti*/
	public int registration();
	
	/** Permette ad un client di loggarsi
	 * @return un pacchetto contenente il token e la lista di richieste di amicizia pendenti se il login ha successo, -1 se l'utente risulta già online, -2 se la password è sbagliata, -3 se l'utente non è registrato*/
	public Packet login();
	
	/** Permette ad un client di inviare una richiesta di amicizia ad un altro 
	 * @param name nome dell'utente a cui inviare la richiesta
	 * @return 0 se l'invio ha successo, -1 se il token non è valido, -2 se l'utente a cui si vuole inviare la richiesta non esiste o non è online o gli era già stata inviata una richiesta o l'utente a cui si vuole inviare è il mittente stesso*/
	public int requestFriendship(String name);
	

	/** Metodo per confermare richieste di amicizia
	 * @param username nome dell'utente da confermare come amico
	 * @return -1 se il token non è più valido, -2 se sono già amici, -3 se la richiesta non esiste, 0 se la conferma ha successo */
	public int confirmRequest(String username);
	
	/** Metodo per cancellare richieste di amicizia
	 * @param username nome dell'utente da non confermare come amico
	 * @return -1 se il token non è più valido, -2 se sono già amici, -3 se la richiesta non esiste, 0 se la cancellazione ha successo */
	public int deleteRequest(String username);
	
	/** Metodo per ottenere la lista degli amici dell'utente
	 * 	@return una lista di stringhe "NOMEAMICO - STATO" se il token non è valido, null altrimenti*/
	public ArrayList<String> requestFriendList();
	
	/** Metodo per cercare uno o più utenti
	 *  @param name stringa da cercare
	 * 	@return una lista di nomi utente che contengono la stringa name se il token è valido, null altrimenti*/
	public ArrayList<String> searchUser(String name);
	
	/** Metodo che permette ad un utente di pubblicare qualcosa che sarà visibile a tutti i suoi follower
	 * @param content stringa contenente le cose da pubblicare
	 * @return 0 se ha successo la pubblicazione, -1 se il token non è valido*/
	public int publish(String content);
	
	/**Metodo che consente ad un utente di diventare follower di un altro
	 * @param name utente da seguire
	 * @return 0 se ha successo, -1 se il token non è valido,-2 se l'utente da seguire non esiste,-3 se non sono amici*/
	public int follow(String name);
	
	/**Metodo per togliere l'utente dallo stato online
	 * @return 0 se tutto è andato bene*/
	public int logout();
	
	/** Controlla la validità del token nel client (se sono passate 24 ore o meno dall'assegnazione)
	   @return false se il token non è più valido,true se lo è*/
	public boolean controlToken();
}