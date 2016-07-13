package Others;

import java.io.*;
import java.net.*;

public class SocketStructure {
	/** Classe usata come "struttura di comodo" per lavorare con Socket e ObjectInput/OutputStream*/
	public Socket s;/**Socket*/
	public ObjectInputStream in;/**Stream di input*/
	public ObjectOutputStream out;/**Stream di output*/

	/**Costruisce una socket, un ObjectOutputStream e un ObjectInputStream per comunicare
	 	@param port porta da usare per la comunicazione con la socket*/
	public SocketStructure(int port) throws UnknownHostException, IOException, SocketException{
		this.s=new Socket(InetAddress.getLocalHost(),port);
		this.s.setSoTimeout(100000);
		this.s.setTcpNoDelay(true);		
		this.out= new ObjectOutputStream(this.s.getOutputStream());
		this.in= new ObjectInputStream(this.s.getInputStream());
	}

	/** Manda una pacchetto attraverso la socket
	 	@param p pacchetto da spedire*/
	public void send(Packet p) throws IOException{
		this.out.writeUnshared(p);
	}
	
	/** Riceve un oggetto attraverso la socket
	 * @return l'oggetto suddetto*/
	public Object receive() throws IOException, ClassNotFoundException {
		return this.in.readUnshared();
	}
	
	/** Chiude la socket e gli stream usati*/
	public void closeStructure() throws IOException{
		if(this.in!=null) in.close();
		if(this.out!=null) out.close();
		if(this.s!=null) this.s.close();
	}
}