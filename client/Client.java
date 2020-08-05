package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	void submitAnswerPut(Integer key, String previousValue) throws RemoteException;
	void submitAnswerGet(Integer key, String value) throws RemoteException;
}