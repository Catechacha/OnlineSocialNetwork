package Server;

import Others.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**Questa è la classe "cuore" del server. Ogni volta che arriva una richiesta, il server crea un handler: questo guarda qual è il tipo del pacchetto
 * e fa diverse azioni a seconda di questo*/
public class ClientHandler implements Runnable{
	Socket client;
	
	/**Costruisce l'handler
	 * @param c socket passatagli dal main del server*/
	public ClientHandler(Socket c){
		this.client=c;
	}
	
	@Override
	public void run(){
		try(ObjectInputStream in= new ObjectInputStream(this.client.getInputStream());
				ObjectOutputStream out= new ObjectOutputStream(this.client.getOutputStream());){
			Packet p=(Packet) in.readUnshared();//ricevi un pacchetto
			Packet x = null;//pacchetto di risposta
			//se il tipo di p non è registrazione, login, logout, requestmessage (quelli dove non devo controllare il token)
			if(p.getType()!=Packet.Type.REGISTRATION && p.getType()!=Packet.Type.LOGIN && p.getType()!=Packet.Type.LOGOUT && p.getType()!=Packet.Type.REQUESTMESSAGES){//per queste operazioni non devo controllare il token
				if(!SocialServer.u.controlToken(p.getToken()))//se il token non è valido
					x=new Packet(Packet.Type.TOKENERROR);//il pacchetto di risposta sarà di tipo tokenerror
				else{
					x=new Packet(Packet.Type.RESPONSE);//altrimenti sarà di risposta normale
				}
			}
			if(x==null | (x!=null && !(x.getType().equals(Packet.Type.TOKENERROR)))){
				switch(p.type){
				case REGISTRATION:{
					x=new Packet(Packet.Type.RESPONSE);
					x.addData(SocialServer.u.addUser(p.getSpecificData(0),p.getSpecificData(1)));
					break;
				}
				case LOGIN:{
					x=new Packet(Packet.Type.RESPONSE);
					x.addData(SocialServer.u.login(p.getSpecificData(0),p.getSpecificData(1),p.getSpecificData(2)));
					if(x.getSpecificData(0)!="-1"&&x.getSpecificData(0)!="-2"&&x.getSpecificData(0)!="-3"){//se il login è andato bene mando le richieste di amicizia pendenti
						ArrayList<String> request=SocialServer.u.getUserList().get(p.getSpecificData(0)).getRequestListOnlyUser();
						for(String s: request)
							x.addData(s);
					}
					break;
				}
				case LOGOUT:{
					SocialServer.u.logout(p.getSpecificData(0));
					break;
				}
				case SEARCH:{
					x.setData(SocialServer.u.search(p.getSpecificData(0)));
					break;
				}
				case FRIENDREQUEST:{
					String senderName=p.getSpecificData(0);//nome di chi vuole inviare la richiesta
					String username=p.getSpecificData(1);//nome dell'utente a cui si vuole mandare la richiesta
					if(!SocialServer.u.controlToken(p.getToken())){//il token non è valido: invio -1
						x.addData(-1);
					}else{
						if(!SocialServer.u.existUser(username)||(SocialServer.u.existUser(username)&&(!SocialServer.u.isOnline(username)||SocialServer.u.areFriends(senderName,username)||SocialServer.u.requested(senderName,username)))||senderName.equals(username))
						//l'username non esiste o l'utente esiste ma non è online o sono già amici o l'utente gli ha già inviato una richiesta di amicizia
							x.addData(-2);
						else{
							SocialServer.u.validateRequest(username);//funzione che aggiorna le richieste di amicizia di un utente eliminando quelle non più valide
							int port = SocialServer.u.getUserList().get(username).getRequestPORT();//guardo se l'utente è online con una socket che si connette a lui
							SocketStructure s;
							try{
								s=new SocketStructure(port);
							}catch(IOException | NullPointerException e){
								x.addData(-2);
								s=null;
							}
							if(s!=null){
								Packet toSend =new Packet(Packet.Type.FRIENDREQUEST);//gli mando la richista con il nome dell'utente che gliel'ha mandata, così può visualizzarla nell'interfaccia
								toSend.addData(senderName);
								s.send(toSend);
								SocialServer.u.getUserList().get(username).addRequest(senderName);//scrivi il nome del sender nella lista di richieste dell'utente username
								x.addData(0);
								s.closeStructure();
							}
						}	
					}
					break;
				}
				case CONFIRMREQUEST:{
					String username=p.getSpecificData(0);//nome di chi vuole confermare la richiesta
					String senderName=p.getSpecificData(1);//nome dell'utente che aveva mandato la richiesta
					SocialServer.u.validateRequest(username);//funzione che aggiorna le richieste di amicizia di un utente eliminando quelle non più valide
					if(SocialServer.u.requested(senderName,username)){//se la richiesta esiste
						if(!SocialServer.u.areFriends(senderName,username)){
							/*guardo che non siano amici perchè potrebbe essersi verificato il seguente caso: A ha richiesto amicizia a B e B l'ha
							 * richiesta ad A. Se A conferma l'amicizia a B, diventano amici, ma cancello solo la richiesta nella lista di A.
							 * Quando poi B va a confermare la richiesta che ha ricevuto, ciò deve essere impossibile in quanto sono già amici,
							 * però devo cancellare comunque la richiesta dalla lista di B*/
							SocialServer.u.setFriends(senderName,username);//conferma l'amicizia
							x.addData(0);
						}else
							x.addData(-2);//sono già amici
						SocialServer.u.removeRequest(username,senderName);//elimina la richiesta d'amicizia dalla lista
					}else
						x.addData(-3);//la richiesta non esiste
					break;
				}case DELETEREQUEST:{
					String username=p.getSpecificData(0);//nome di chi vuole cancellare la richiesta
					String senderName=p.getSpecificData(1);//nome dell'utente che aveva mandato la richiesta
					SocialServer.u.validateRequest(username);//funzione che aggiorna le richieste di amicizia di un utente eliminando quelle non più valide
					if(SocialServer.u.requested(senderName,username)){//se la richiesta esiste
						if(!SocialServer.u.areFriends(senderName,username))
							x.addData(0);//la rimozione della richiesta ha successo e non sono amici
						else
							x.addData(-2);//sono già amici, ma tolgo comunque la richiesta dalla lista 
							/*potrebbe essersi verificato il caso seguente: A ha fatto richiesta di amicizia a B, e viceversa. A ha accettato 
							 * la richiesta di B quindi sono amici. B vuole cancellare la richiesta di A: la richiesta viene quindi cancellata
							 * e inoltre viene comunicato a B che risultano già amici. */
						SocialServer.u.removeRequest(username,senderName);//elimina la richiesta d'amicizia dalla lista
					}else
						x.addData(-3);//la richiesta non esiste
					break;
				}
				case FRIENDLIST:{//richiesta lista amici
					x.setData(SocialServer.u.friendList(p.getSpecificData(0)));
					break;
				}
				case PUBLISH:{//richiesta di pubblicazione di un contenuto
					SocialServer.u.publish(p.getSpecificData(0),p.getSpecificData(1));
					x.addData(0);
					break;
				}
				case REQUESTMESSAGES:{//l'utente si è appena collegato e vuole sapere se ci sono messaggi degli utenti che segue che non ha visualizzato
					String username=p.getSpecificData(0);
					CopyOnWriteArrayList<String> messList=SocialServer.u.getUserList().get(username).getMessages();
					CallBackInterface callb=SocialServer.u.getUserList().get(username).getCallback();
					if(messList!=null && messList.size()!=0)//se ci sono messaggi --> mandaglieli con RMI
						for(String s:messList)
							callb.sendMessage(s);
					SocialServer.u.getUserList().get(username).resetMessages();//poi svuota la lista dei messaggi
					break;
				}
				default://altri pacchetti vengono ignorati
					break;
				}
			}
			if(p.type!=Packet.Type.LOGOUT)
				out.writeUnshared(x);
		}catch(IOException e){
			System.err.println("ClientHandler: Some error appeared");					
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");	
		}
	}
}