package Client;
import Others.*;
import java.io.*;
import java.net.*;

import javax.swing.SwingUtilities;

/**Runnable che mi aspetta le richieste di amicizia che arrivano agli utenti. Viene avviato una volta fatto il login*/
public class FriendRequestRunnable implements Runnable{
	public ServerSocket s;/**Socket sulla quale si attendono richieste di connessione*/
	public ClientGui gui;/**Interfaccia grafica dell'utente che ha avviato il thread*/
	
	/**costruttore*/
	public FriendRequestRunnable(ServerSocket ss, ClientGui gui){
		this.s=ss;
		this.gui=gui;
	}
	
	@Override
	public void run() {
		while(true){
			try(Socket client=s.accept();
					ObjectInputStream in= new ObjectInputStream(client.getInputStream());
					ObjectOutputStream out= new ObjectOutputStream(client.getOutputStream());){
				Packet p=(Packet) in.readUnshared();
				if(p.getType().equals(Packet.Type.FRIENDREQUEST)){//se il pacchetto è di tipo friendRequest, mostra il nome di chi ti ha fatto la richiesta nell'interfaccia
					RequestShower shower = new RequestShower(this.gui,p.getSpecificData(0));
					SwingUtilities.invokeLater(shower);
				}else
					if(p.getType().equals(Packet.Type.STOP))
						break;//se il pacchetto è di tipo STOP il thread deve fermarsi
				//altri tipi di pacchetti vengono ignorati automaticamente
			}catch(IOException e){
				System.err.println("Some error appeared");					
			} catch (ClassNotFoundException e) {
				System.err.println("Class Packet not found");	
			}
		}
	}
}