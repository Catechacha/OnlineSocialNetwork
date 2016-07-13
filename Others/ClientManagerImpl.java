package Others;
import Server.*;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**Implementazione dell'interfaccia remota*/
public class ClientManagerImpl extends RemoteObject implements ClientManager{
	private static final long serialVersionUID = 1L;
	UserList userList;/**lista utenti che prende dal server*/

	public ClientManagerImpl(){
		this.userList=SocialServer.u;
	}

	@Override
	public int registerCallback(String username, String token,CallBackInterface stub) throws RemoteException {
		if(this.userList.controlToken(token)){
			this.userList.getUserList().get(username).setCallback(stub);
			return 0;
		}else
			return -1;
	}

	
	@Override
	public int follow(String token,String follower,String toFollow) throws RemoteException {
		if(this.userList.controlToken(token)){
			if(this.userList.existUser(toFollow)){
				if(this.userList.areFriends(follower, toFollow)){
						if(!this.userList.isFollower(follower,toFollow)){
							this.userList.getUserList().get(toFollow).addFollower(follower);
							return 0;
						}else
							return -4;
				}else
					return -3;
			}else
				return -2;
		}
		return -1;
	}
}