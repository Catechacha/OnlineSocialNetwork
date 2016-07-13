package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class KeepAliveServerWriter implements Runnable{
	/**Questo thread prende la lista degli utenti e mette il flag alive=false per ogni user (chiamando la funzione prepareToKeepAlive)
	 * Quindi manda i messaggi e poi dorme per 10 secondi. Richiama la funzione updateList sulla lista degli utenti che fa fare logout a quegli utenti
	 * il cui flag  alive è false (perchè sono quelli che non hanno risposto: se fosse arrivata una loro risposta il thread KeepAliveServerReader
	 * avrebbe settato il flag a true). */
	
	static Long sendTime;/**Tempo in cui il server manda il messaggio agli utenti
	Thread t;/**Thread usato per leggere le risposte degli utenti ai messaggi di keepalive*/
	MulticastSocket ms;/**Multicast socket usata per mandare messaggi multicast*/
	InetAddress group;/**Usato per il gruppo multicast*/
	DatagramPacket p;/**Pacchetto da mandare agli utenti del gruppo*/
	UserList u;/**Lista di utenti ricevuta dal server*/

	/**Costruttore del thread che manda messaggi di keepAlive
	 * @param port porta su cui mandare i messaggi 
	 * @param ip per il gruppo multicast
	 * @param receiverPort porta su cui poi il server riceverà messaggi
	 * @param u lista utenti*/
	public KeepAliveServerWriter(int port,String ip, int receiverPort,UserList u){
		try {
			ms = new MulticastSocket();
			group = InetAddress.getByName(ip);
			String toSend="Are you alive?";
			p= new DatagramPacket(toSend.getBytes(),toSend.getBytes().length,group,port);	
		} catch (IOException e) {
			System.err.println("An error occured in creation of multicast socket");
		}
		this.u=u;
		//faccio partire il thread per leggere le risposte
		Thread t=new Thread(new KeepAliveServerReader(receiverPort,u));
		t.start();
	}

	@Override
	public void run() {
		while(true){
			try {
				u.prepareToKeepAlive();//preparo gli utenti per il keepalive: metto a false il flag alive per ognuno, e poi setto a true solo quelli che mi rispondono
				ms.send(p);//manda il pacchetot
				sendTime=System.currentTimeMillis();//prendi il tempo a cui l'hai mandato (più o meno)
				Thread.sleep(10000);//dormi 10 secondi
			} catch (IOException e) {
				System.err.println("An error occured sending messages in multicast socket");
			} catch (InterruptedException e) {}
			u.updateList(); //aggiorna la lista: setta offline tutti gli utenti con flag alive=false
		}
	}	
}