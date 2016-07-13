package Others;

import java.io.Serializable;

public class Request implements Serializable/*per il salvataggio delle richieste devono essere serializzabili*/{
	private static final long serialVersionUID = 1L;
	/**Classe usata per le richieste di amicizia: ogni richiesta è caratterizzata dall'utente che l'ha mandata e dal tempo in cui è arrivata*/
	private long time;/**Tempo in millisecondi in cui il Server ha ricevuto la richiesta*/
	String user;/**Utente che ha mandato la richiesta*/
	
	/**Costruttore: assegna l'utente e salva il tempo in cui è stata pervenuta la richiesta
	 * @param user utente da assegnare*/
	public Request(String user){
		this.user=user;
		this.setTime(System.currentTimeMillis());
	}

	/**Setter del tempo
	 * @param il tempo a cui è arrivata la richiesta*/
	private void setTime(long t) {
		time =t;
	}

	/**Getter dell'user
	 * @return il nome dell'utente che ha fatto la richiesta*/
	public String getUser() {
		return user;
	}

	/**Getter del tempo
	 * @return il tempo a cui è arrivata la richiesta*/
	public long getTime() {
		return time;
	}
}