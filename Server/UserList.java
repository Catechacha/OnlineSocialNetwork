package Server;

import Others.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserList implements Serializable {
	private static final long serialVersionUID = 1L;
	/**Struttura dati che memorizza ciò il server deve mantenere*/
	private ConcurrentHashMap<String,User> userList;/**Lista di utenti*/

	/**Crea una nuova lista di utenti vuota*/
	public UserList(){
		setUserList(new ConcurrentHashMap<String,User>());
	}

	/**Salva la lista utenti sul file userListFile.dat*/
	public void save() {
		ObjectOutputStream writeOnFile=null;
		try {
    		File userListFile = new File((String)(SocialServer.PATHFILE));//file dove sta la lista utenti
    		writeOnFile= new ObjectOutputStream(new FileOutputStream(userListFile));
			writeOnFile.writeObject(this);//salvala
        } catch (IOException | NullPointerException e) {
            System.err.println("User list save error");
        }finally{
        	if(writeOnFile!=null)
				try {writeOnFile.close();} catch (IOException e) {System.err.println("User list save error");}
        }
	}
	 
	/** Aggiunge un nuovo utente alla lista utenti 
	  * @param name string contenente il nome dell'utente da aggiungere
	  * @param password stringa contenente la password dell'utente da aggiungere
	  * @return 0 se l'aggiunta è andata a buon fine, -1 altrimenti */
	public int addUser(String name, String password){
		if(!existUser(name)){
			getUserList().put(name,new User(name,password));
			save();//salvo perchè ho creato un nuovo utente
			return 0;
		}
		return -1;
	}

	/** Metodo che controlla se esiste già un utente con un certo nome o meno 
	  * @param     name  nome utente da controllare
	  * @return    true se esiste già l'username name, false altrimenti */
	public boolean existUser(String name){
		if(getUserList().containsKey(name))
			return true;
		return false;
	}
	
	 /** Autentica le credenziali dell'utente e gli fornisce un token
	  * @param name  nome utente che vuole loggarsi
	  * @param password password dell'utente che vuole loggarsi
	  * @param requestPORT porta su cui il client attende richieste di amicizia
	  * @return token se il login ha avuto successo, -1 se l'utente risulta già online, -2 se la password è sbagliata, -3 se l'utente non è registrato*/
	public String login(String name, String password, String requestPORT){
		if(existUser(name)){
			if(this.getUserList().get(name).getPassword().equals(password)){
				if(this.getUserList().get(name).getToken()==null){
					String token=this.generateNewToken();
					this.getUserList().get(name).setToken(token);//setta il token dell'utente
					this.getUserList().get(name).setRequestPORT(Integer.parseInt(requestPORT));//salva la request port dove l'utente attende le richieste di amicizia
					this.getUserList().get(name).setAlive(true);//setta l'utente come alive
					save();//salvo lo stato della lista in quanto c'è un nuovo utente che si è loggato
					return token;
				}else
					return "-1";
			}else
				return"-2";
		}else
			return"-3";
	}
	
	 /** Metodo che genera un token (stringa casuale di 10 caratteri)
	    @return	un token */
	private String generateNewToken() {
		Random rnd = new Random ();
		char[] arr = new char[10];
		boolean valid=true;
		String token;
		do{
			for (int i=0; i<10; i++) {//creo un token casuale
				int n = rnd.nextInt(36);
				arr[i] = (char) (n < 10 ? '0'+n : 'a'+n-10);
			}
			valid=true;
			token=String.valueOf(arr);
			Set<String> keySet = this.getUserList().keySet(); //verifico che nessun utente online abbia già il token appena creato
			for(String u:keySet){
				if(this.getUserList().get(u).getToken()!=null)
					if(this.getUserList().get(u).getToken().equals(token))
						valid=false;
			}
		}while(!valid);
		return token;
	}

	/** Rimuove il token associato ad un utente
	 * @param name username dell'utente
	 * @param t token dell'utente */
	public void logout(String name) {
		this.getUserList().get(name).resetUser();
		save();//salvo lo stato
	}

	/** Ritorna i nomi utente che contengono una certa stringa
	 * @param name stringa da cercare
	 * @return un'array con tutti i nomi utente che contengono name*/
	public ArrayList<String> search(String name) {
		ArrayList<String> a = new ArrayList<String>();
		Set<String> keySet = this.getUserList().keySet();
		for(String u:keySet){
			if(u.contains(name)|u.equals(name))
				a.add(u);
		}
		return a;
	}

	/** Controlla se un token è valido (se esiste o meno nella lista)
	 * @param token da controllare
	 * @return true se il token è valido, false altrimenti */
	public boolean controlToken(String token) {
		Set<String> keySet = this.getUserList().keySet();
		for(Object key:keySet){
		     User value = this.getUserList().get(key);
		     try{
		     if(value.getToken().equals(token))
		    	 return true;
		     }catch(NullPointerException e){}
		}
		return false;
	}
	
	/**Metodo che controlla se due utenti sono amici
	 * @param a nome del primo utente
	 * @param b nome del secondo utente
	 * @return true se sono amici, false se non lo sono */
	public boolean areFriends(String a,String b){
		CopyOnWriteArrayList<String> friends = this.getUserList().get(a).getFriendlist();
		if(friends==null||friends.size()==0)
			return false;
		if(friends.contains(b))
			return true;
		return false;
	}
	
	/**Metodo che controlla se un utente è online
	 * @param u nome utente da controllare
	 * @return true se è online, false altrimenti */
	public boolean isOnline(String u){
		if(this.getUserList().get(u).getToken()==null)
			return false;
		return true;
	}
	
	/** Metodo che controlla se un utente ha già fatto richiesta di amicizia ad un altro
	 * @param senderName nome dell'utente richiedente amicizia
	 * @param username nome dell'utente a cui viene richiesta amicizia
	 * @return true se senderName ha già richiesto l'amicizia a username, false altrimenti */
	public boolean requested(String senderName, String username) {
		ArrayList<String> request = this.getUserList().get(username).getRequestListOnlyUser();
		if(request==null||request.size()==0)
			return false;
		if(request.contains(senderName))
			return true;
		return false;
	}

	/** Metodo che rende due utenti amici
	 * @param a utente
	 * @param b utente	 */
	public void setFriends(String a, String b) {
		this.getUserList().get(a).addFriend(b);
		this.getUserList().get(b).addFriend(a);
		save();////salvo perchè ho creato una nuova amicizia
	}
	
	/** Ritorna la lista degli amici dell'utente username con stato (online/offline)
	 * @param username nome dell'utente interessato
	 * @return un'array di nomi utente, ognuno con proprio stato*/
	public ArrayList<String> friendList(String username){
		ArrayList<String> a = new ArrayList<String>();
		CopyOnWriteArrayList<String> list = this.getUserList().get(username).getFriendlist();
		for(String s:list){
			if(isOnline(s))
				a.add(s+" - ONLINE");
			else
				a.add(s+" - OFFLINE");
		}
		return a;
	}

	/** Metodo che rimuove la richiesta di amicizia da parte dell'utente senderName nella lista delle richieste di username
	 * @param username nome dell'utente in cui togliere l'elemento dalla lista
	 * @param senderName nome dell'utente da cancellare dalla lista */
	public void removeRequest(String username, String senderName) {
		this.getUserList().get(username).removeRequest(senderName);
		save();////salvo perchè ho eliminato una richiesta
	}

	/**Funzione che aggiorna le richieste di amicizia di un utente eliminando quelle non più valide (quelle per le quali è trascorso il 
	 * TIMEFRIENDREQUEST specificato nel server e caricato dal file di configurazione)
	 * @param username nome dell'utente del quale aggiornare la lista */
	public void validateRequest(String username) {
		CopyOnWriteArrayList<Request> requestList=this.getUserList().get(username).getRequestList();
		for(int i=0;i<requestList.size();i++){
			if((System.currentTimeMillis()-requestList.get(i).getTime())>SocialServer.TIMEFRIENDREQUEST){
				removeRequest(username,requestList.get(i).getUser());
				save();//salvo perchè ho aggiornato la lista di richieste valide
			}
		}
	}

	/**Procedura che mi permette di inviare alla lista di follower di un utente ciò che lui vuole pubblicare. Se un follower non è online, 
	 * il messaggio viene messo in una lista e poi sarà inviato quando questo torna online
	 * @param username utente che vuole pubblicare
	 * @param content contenuto da pubblicare*/
	//NOTA: è void perchè il controllo del token avviene prima di chiamare questa funzione
	public void publish(String username, String content) {
		CopyOnWriteArrayList<String> followerList =this.getUserList().get(username).getFollower();
		for(int i=0;i<followerList.size();i++){
			String followerName=this.getUserList().get(username).getFollower().get(i);
			CallBackInterface a=this.getUserList().get(followerName).getCallback();
			if(a!=null)
				try{
					a.sendMessage(username+": "+content+"\n");
				}catch(RemoteException e){
					System.err.println("Comunication error with RMI");
				}
			else{
				//se l'utente non è online metto il messaggio nella sua lista di messaggi da leggere
				SocialServer.u.getUserList().get(followerName).addMessage(username+": "+content+"\n"); //altrimenti inserisco il messaggio nei messaggi da leggere
				save();//salvo la lista perchè ho aggiunto un mess
			}
		}
	}
	
	/**Metodo che mi dice se un utente è follower di un altro
	 * @param follower utente che vuole diventare follower
	 * @param toFollow utente che il follower vuole seguire
	 * @return true se è gia follower, false altrimenti */
	public boolean isFollower(String follower, String toFollow) {
		return this.getUserList().get(toFollow).getFollower().contains(follower);
	}

	/**Permette di settare un utente online o meno per i messaggi di keepalive*/
	public void setAlive(String usernameReceive,boolean value) {
		getUserList().get(usernameReceive).setAlive(value);
	}

	/**Setta il flag alive di tutti gli utenti a false*/
	public void prepareToKeepAlive() {
		Set<String> keySet = this.getUserList().keySet();
		for(String key:keySet)
		     getUserList().get(key).setAlive(false);	
	}

	/**Aggiorna la lista di utenti online*/
	public void updateList() {
		Set<String> keySet = this.getUserList().keySet();
		for(String key:keySet){
			User x=getUserList().get(key);
		    if((x.getAlive()==false && x.getToken()!=null||(x.getAlive()==true &&x.getToken()==null)))
		    	//se l'utente non ha risposto al mess di keepalive e se ha un token cioè risulterebbe online
		    	//oppure se l'utente è risponde ai messaggi di keepalive ma non ha un token (caso che potrebbe verificarsi con il ripristino
		    	//della lista utenti dopo un crash del server
		    	x.resetUser();//rimuovo il suo token rendendolo così offline
	    }	
	}

	/**In caso di crash del server, dal salvataggio viene recuperata la lista utenti, ma li metto tutti offline*/
	public void resetUserList() {
		Set<String> keySet = getUserList().keySet();
		for(String key:keySet){
			User x = getUserList().get(key);
			x.resetUser();
		}
	}
	
	/**Getter della lista utenti*/
	public ConcurrentHashMap<String,User> getUserList() {
		return userList;
	}

	/**Setter della lista utenti*/
	public void setUserList(ConcurrentHashMap<String,User> userList) {
		this.userList = userList;
	}
}