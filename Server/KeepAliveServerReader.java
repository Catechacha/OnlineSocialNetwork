package Server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class KeepAliveServerReader implements Runnable{
	/**Questo thread riceve i messaggi di keepalive degli utenti, contenenti il loro token. Ogni volta che ne riceve uno, se il messaggio è arrivato
	 * in tempi corretti, setta il flag alive a true
	 */
	DatagramSocket ds;/**Usata per ricevere messaggi*/
	UserList u;/** Lista degli utenti del server*/

	/**Costruttore*/
	public KeepAliveServerReader(int port,UserList u){
		//apro una datagramSocket sulla porta usata per la ricezione di messaggio di keepalive
		try {
			ds=new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.println("An error occured in creation of DatagramSocket");
		}
		this.u=u;//prendo la lista degli utenti dal server
	}
	
	@Override
	public void run() {
		DatagramPacket p;
		while(true){
			p = new DatagramPacket(new byte[512], 512);
			try {
				ds.receive(p);
			} catch (IOException e) {
				System.err.println("An error occured working with DatagramSocket");
			}
			if((System.currentTimeMillis()-KeepAliveServerWriter.sendTime)<10000){
				byte[] usernameInByteArray=p.getData();
				String usernameReceived=null;
				try {
					usernameReceived = new String(usernameInByteArray, "UTF-8").trim();
				} catch (UnsupportedEncodingException e) {
					System.err.println("Encoding error");
				}
				if(u.isOnline(usernameReceived))//se l'utente è online (nel frattempo potrebbe aver fatto logout) setta alive a true, altrimenti lascialo false
					u.setAlive(usernameReceived,true);
			}
		}
	}
}