package peer;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.server.UnicastRemoteObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentNavigableMap;

import client.Client;
import manager.Manager;

public class PeerImplementation implements Peer {
	private Manager manager;
	private ConcurrentSkipListMap<Integer, String> table;
	private ConcurrentSkipListMap<Integer, Peer> myPeers;
	private int hashID;

	public PeerImplementation(Registry registry, Manager manager) {
		super();

		this.manager = manager;
		this.table = new ConcurrentSkipListMap<Integer, String>();
	}

	private void moveHelper(Peer destination, int begin, boolean includeBegin, int end, boolean includeEnd){
		ConcurrentNavigableMap<Integer, String> toMoveTable = this.table.subMap(begin, includeBegin, end, includeEnd);

		for(Map.Entry<Integer, String> entry: toMoveTable.descendingMap().entrySet()){
			try{
				destination.put(entry.getKey(), entry.getValue(), null);
			}
			catch(RemoteException exception){
				System.err.print("Error moving entry to destination");
				exception.printStackTrace();
			}
		}
		toMoveTable.clear();
	}

	public void move(int newHashID, Peer destination){	//we don't need currHashID, only newHashID
		int currHashID = this.hashID;

		if(currHashID == newHashID) return;

		if(currHashID < newHashID) moveHelper(destination, currHashID, false, newHashID, true);
	
		else{
			if(this.table.isEmpty()) return;
			moveHelper(destination, currHashID, false, this.table.lastKey(), true);
			moveHelper(destination, this.table.firstKey(), true, newHashID, true);
		}
	}
	
	public void copyAll(Peer destination){
		try{
			for (Entry<Integer, String> entry : this.table.entrySet()){
				destination.put(entry.getKey(), entry.getValue(), null);
			}
		}
		catch(RemoteException exception){
			System.err.print("Could not copy all keys to destination.\n");
		}
	}

	private Peer findPredecessor(Integer hashKey){
		Entry<Integer, Peer> entry = myPeers.lowerEntry(hashKey);
		if(entry != null) return entry.getValue();

		entry = myPeers.lastEntry();

		return (entry != null) ? entry.getValue() : null;
	}

	public void flush(Integer id1, Integer id2, Integer id3){
		for(Entry<Integer,String> entry : table.entrySet()){
			try{
				Peer authPeer = find(entry.getKey());
				int authID = authPeer.getHashID();
				if(!(authID == id1 || authID == id2 || authID == id3)) this.table.remove(entry.getKey());
			}
			catch(RemoteException exception){
				System.err.print("Could not copy all keys to destination.\n");
			}
		}
	}
	public void updateData(){
		try{
			Peer curr = find(this.hashID);
			Peer pred1 = findPredecessor(this.hashID);	//if it is the first it will return itself
			if(pred1 == null || pred1.getHashID() == curr.getHashID()) return;	//after this point we are sure that pred1 != pred2

			Peer pred2 = findPredecessor(pred1.getHashID());
			if(pred2 == null || pred2.getHashID() == pred1.getHashID() || pred2.getHashID() == curr.getHashID()) return;
			
			pred1.copyAll(curr);
			pred2.copyAll(curr);

			curr.flush(pred1.getHashID(), pred2.getHashID(), curr.getHashID());

		}
		catch(RemoteException exception){
			System.err.println("Error copying.");
		}
	}

	public void putReplica(Integer key, String value, Client client){
		try {
			if(client != null) {
				client.submitAnswerPut(key, table.put(key, value));
			}
		}
		catch(RemoteException exception) {
			System.err.print("Cannot contact client: ");
			exception.printStackTrace();
		}
	}

	public void put(Integer key, String value, Client client) {
		Peer destination = find(key);	//we get the correct peer based on the key

		if(destination == this) {	//if we are at the right destination then 
			try {
				if(client != null) {
					client.submitAnswerPut(key, table.put(key, value));
				}
			}
			catch(RemoteException exception) {
				System.err.print("Cannot contact client: ");
				exception.printStackTrace();
			}
		}
		else {	//if we are not in the right destination then
			try {
				if(destination != null) {	//if the destination is not null
					destination.putReplica(key, value, client);	//then done
				}
				else {
					System.err.println("Error finding destination peer");
				}
			}
			catch(Exception exeption) {
				System.err.println("Error contacting destination peer");
			}
		}
	}

	public void getReplica(Integer key, Client client){
		try {
			if(client != null) {
				client.submitAnswerGet(key, table.get(key));
			}
		}
		catch(RemoteException exception) {
			System.err.print("Cannot contact client: ");
			exception.printStackTrace();
		}
	}

	public void get(Integer key, Client client) {
		Peer destination = find(key);

		if(destination == this) {
			try {
				if(client != null) {
					client.submitAnswerGet(key, table.get(key));
				}
			}
			catch(RemoteException exception) {
				System.err.print("Cannot contact client: ");
				exception.printStackTrace();
			}
		}
		else {
			try {
				if(destination != null) {
					destination.getReplica(key, client);
				}
				else {
					System.err.println("Error finding destination peer");
				}
			}
			catch(Exception exeption) {
				System.err.println("Error contacting destination peer");
			}
		}
	}

	public Boolean ping() {
		System.out.println("Tum-Tum");
		return true;
	}

	public ArrayList<Peer> getCurrentPeers() {
		ArrayList<Peer> result = new ArrayList<Peer>();

		for(Map.Entry<Integer, Peer> entry: myPeers.entrySet()) {
			result.add(entry.getValue());
		}

		return result;
	}

	public void addMember(Peer peer){
		try{
			this.myPeers.put(peer.getHashID(), peer);
		}
		catch(RemoteException exception){
			System.err.println("Unable to add member.");
			return;
		}
		
	}

	public void updateMembers(Peer newMember) {
		System.out.println("Updating member list");

		ConcurrentSkipListMap<Integer, Peer> updatedPeers = new ConcurrentSkipListMap<Integer, Peer>();	//will be this peers list of peers
		ArrayList<Peer> peers = null;	//will be current list of peers

		try {
			Peer randomPeer = manager.getRandom();	//we obtain a random peer reference
			if(randomPeer != null){
				peers = randomPeer.getCurrentPeers();	//we get current peers from this peers
				for(Peer peer: peers){
					updatedPeers.put(peer.getHashID(), peer);
					peer.addMember(newMember);
				}
			}
			updatedPeers.put(this.hashID, newMember);
			myPeers = updatedPeers;
		}
		catch (RemoteException exception) {
			System.err.println("Unable to contact manager to update members.");
			return;
		}
	}

	private Peer find(Integer key) {
		Entry<Integer, Peer> entry = myPeers.ceilingEntry(Math.abs(key.hashCode() % (2 << 16)));
		if(entry != null) return entry.getValue();

		entry = myPeers.firstEntry();
		return (entry != null) ? entry.getValue() : null;
	}
	
	public void setHashID(int hashID){
		this.hashID = hashID;
	}

	public int getHashID(){
		return this.hashID;
	}

	public void join(Manager manager) {
		try {
			Peer peerStub = (Peer) UnicastRemoteObject.exportObject(this, 0);	//remote object that can be accessed by others
			this.setHashID(Math.abs(peerStub.hashCode() % (2<<16)));
			manager.register(peerStub);

			Peer successor = find(this.getHashID() + 1);//finds the peer whose keys will be moved to this new peer
			if(successor == null) return;
		
			successor.move(this.getHashID(), peerStub);
			this.updateMembers(peerStub);//now we update members for all others
		}
		catch(AccessException exception) {
			System.err.println("Error binding peer into the registry.");
		}
		catch(RemoteException exception) {
			//System.err.println("Error accessing registry.");
			System.err.println(exception.getMessage());
		}
	}

	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.getRegistry();

			Manager manager = (Manager) registry.lookup("DynamoClone");

			PeerImplementation peer = new PeerImplementation(registry, manager);

			peer.join(manager);

			PeriodicAgent agent = new PeriodicAgent(peer);
			agent.start();
		}
		catch(Exception exception) {
			System.err.print("Failed to locate manager in registry: ");
			exception.printStackTrace();
		}
	}
}