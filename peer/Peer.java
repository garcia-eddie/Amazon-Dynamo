package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import client.Client;

public interface Peer extends Remote {
    void put(Integer key, String value, Client client) throws RemoteException;
    void putReplica(Integer key, String value, Client client) throws RemoteException;
    void get(Integer key, Client client) throws RemoteException;
    void getReplica(Integer key, Client client) throws RemoteException;

    void flush(Integer id1, Integer id2, Integer id3) throws RemoteException;
    void move(int newHashID, Peer destination) throws RemoteException;
    void updateData() throws RemoteException;
    void copyAll(Peer peer) throws RemoteException;
    ArrayList<Peer> getCurrentPeers() throws RemoteException;
    void updateMembers(Peer peer) throws RemoteException;
    void addMember(Peer peer) throws RemoteException;
    int getHashID()throws RemoteException;
    void setHashID(int id) throws RemoteException;

    
    Boolean ping() throws RemoteException;
}