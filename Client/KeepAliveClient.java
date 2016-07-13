package Client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class KeepAliveClient implements Runnable{
	/**Runnable che riceve i messaggi in multicast di keepalive e risponde*/
	
	private MulticastSocket ms;/**Per ricevere messaggi di keepAlive*/
	private DatagramSocket ds;/**Per mandare messaggi di keepAlive contenenti l'username dell'utente*/
	private String username;/**username dell'utente che ha avviato questo thread*/
	private InetAddress toSendReply;/**Indirizzo al quale inviare risposte ai messaggi*/
	private int portToReply;/**Porta sulla quale inviare risposte ai messaggi*/
	private InetAddress group;/**indirizzo gruppo multicast*/
	
	public KeepAliveClient(int multicastPort,String host, String username,String hostToSendReply,int portToReply){
		try {
			this.ms=new MulticastSocket(multicastPort);
			this.group = InetAddress.getByName(host);
			this.ms.joinGroup(group);
			this.ds=new DatagramSocket();
			this.toSendReply = InetAddress.getByName(hostToSendReply);
			this.portToReply=portToReply;
			this.username=username;
		} catch (IOException e) {
			System.err.println("An error occured in constructor of KeepAliveClient");
		}
	}

	@Override
	public void run() {
		DatagramPacket p;
		while(true){
			p=new DatagramPacket(new byte[14],14);//faccio p di questa grandezza perchè gli username non possono essere grandi più di 14 caratteri, che è anche la dimensione della stringa "Are you alive?"
			try{
				ms.receive(p);//ricevi un pacchetto dal multicast
			}catch(IOException e){
				System.err.println("An error occured receiving multicast messages");
			}
			byte[] receivedStringInByteArray=p.getData();
			String receivedString = null;
			try {
				receivedString = new String(receivedStringInByteArray,"UTF-8").trim();
			} catch (UnsupportedEncodingException e1) {
				System.err.println("Encoding error");
			}
			if(receivedString.equals("Are you alive?")){//vuol dire che te l'ha mandato il server --> rispondigli mandandogli il tuo username!
				DatagramPacket reply=new DatagramPacket(username.getBytes(),username.getBytes().length,toSendReply,portToReply);
				try {
					ds.send(reply);
				} catch (IOException e) {
					System.err.println("An error occured sending reply to message");
				}
			}else{
				if(receivedString.equals(username)){/* se ho ricevuto il mio username devo terminare*/
					ds.close();
					break;
				}//altre stringhe ricevute sono ignorate automaticamente
			}
		}
		//quando ho finito il mio lavoro esco ovviamente dal gruppo multicast
		try {
			ms.leaveGroup(group);
		} catch (IOException e) {
			System.err.println("An error occured leaving multicast group");
		}
	}
}