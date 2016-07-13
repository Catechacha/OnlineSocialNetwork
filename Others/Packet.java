package Others;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/** E' la classe usata per la comunicazione di richieste e risposte tra client e server*/
public class Packet implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/** Contiene un Type che indica il tipo di pacchetto e un arraylist di dati*/
	public enum Type{RESPONSE,REGISTRATION,LOGIN, LOGOUT, SEARCH, FRIENDREQUEST, TOKENERROR, CONFIRMREQUEST, FRIENDLIST, DELETEREQUEST, PUBLISH, REQUESTMESSAGES, STOP};//vari tipi di pacchetto
	public String token;/**Token del client che manda il pacchetto - null se il pacchetto è mandato dal server*/
	public ArrayList<String> data; /**Dati da mandare nel pacchetto*/
	public Type type;/**Indica che tipo di pacchetto è: se è di risposta, o se richiede operazioni come per esempio la registrazione*/
	
	/** Costruisce un nuovo pacchetto di tipo t vuoto 
	    @param t tipo del pacchetto che si vuole creare
	    @param token token del client che manda il pacchetto*/
	public Packet(Type t, String token) {
		this.type=t;
		this.token=token;
		this.data=new ArrayList<String>();
	}

	/** Costruisce un nuovo pacchetto di tipo t contenente i dati data 
    @param t tipo del pacchetto che si vuole creare
 	@param data dati che si vogliono inserire nel pacchetto*/
	public Packet(Type t,ArrayList<String> data) {
		this.type=t;
		this.data=data;
	}

	/** Costruisce un nuovo pacchetto di tipo t vuoto
    @param t tipo del pacchetto che si vuole creare*/
	public Packet(Type t) {
		this.type=t;
		this.data=new ArrayList<String>();
	}

	/** Aggiunge una Stringa nell'array di dati del pacchetto
   	  	@param data stringa da aggiungere nel pacchetto*/
	public void addData(String data){
		this.data.add(data);
	}

	/** Ritorna i dati contenuti nel pacchetto
	  	@return dati contenuti nel pacchetto*/
	public ArrayList<String> getData() {
		return this.data;
	}
	
	/** Setta il contenuto del pacchetto
	@param a ArrayList di stringhe contenente i dati che si vogliono mettere nel pacchetto*/
	public void setData(ArrayList<String> a) {
		this.data=a;
	}

	/** Aggiunge un intero nell'array di dati del pacchetto
	  	@param i intero da aggiungere nei dati del pacchetto*/
	public void addData(int i) {
		this.data.add(String.valueOf(i));
	}

	/**Ritorna un dato specifico in posizione i nell'array di dati del pacchetto
	 * @param i indice del dato nell'array
	 * @return il dato in posizione i*/
	public String getSpecificData(int i) {
		return this.data.get(i);
	}
	
	/**Ritorna il token associato al pacchetto
	 * @return token associato al pacchetto*/
	public String getToken() {
		return this.token;
	}
	
	/**Ritorna il tipo del pacchetto
	 * @return tipo del pacchetto*/	
	public Type getType(){
		return this.type;
	}

	/**Mette il tutto contenuto di un CopyOnWriteArrayList<String> in un pacchetto
	 * @param messages il CopyOnWriteArrayList */
	public void setData(CopyOnWriteArrayList<String> messages) {
		for(String s:messages)
			this.addData(s);
	}
}