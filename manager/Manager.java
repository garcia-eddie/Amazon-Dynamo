package manager;

import java.rmi.Remote;
import java.rmi.RemoteException;


// import client.Client;
import peer.Peer;

public interface Manager extends Remote {
	// void put(Integer key, String value, Client client) throws RemoteException;
	// void get(Integer key, Client client) throws RemoteException;

	void register(Peer peer) throws RemoteException;
	void unregister(Peer peer) throws RemoteException;
	
	Peer getRandom() throws RemoteException;

}