package manager;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Random;
import java.util.Map.Entry;

import peer.Peer;

public class ManagerImplementation implements Manager {
	private Registry registry;
	private ConcurrentSkipListMap<Integer, Peer> myPeers;

	private Random random;

	public ManagerImplementation(Registry registry) {
		super();

		this.registry = registry;
		this.myPeers = new ConcurrentSkipListMap<Integer, Peer>();

		this.random = new Random();
	}

	public void register(Peer peer) {
		try {
			String peerName = "Peer-" + peer.getHashID();
			registry.rebind(peerName, peer);
			myPeers.put(peer.getHashID(), peer);
		}
		catch(AccessException exception) {
			System.err.println("Error binding peer into the registry.");
		}
		catch(RemoteException exception) {
			System.err.println("Error accessing registry.");
		}
	}

	public void unregister(Peer peer) {
		try {
			int peerID = peer.getHashID();	//used to be peer.hashCode();
			String peerName = "Peer-" + peerID;
			registry.unbind(peerName);
			myPeers.remove(peerID);
		}
		catch(AccessException exception) {
			System.err.println("Error binding peer into the registry.");
		}
		catch(RemoteException exception) {
			System.err.println("Error accessing registry.");
		}
		catch(NotBoundException exception) {
			System.err.println("Error removing peer from the registry.");
		}
	}

	public Peer getRandom() {
		// // Take a random number
		int randomNumber = Math.abs(random.nextInt() % (2 << 16));

		// Finds the largest entry whose key is less or equal to the random number
		Entry<Integer, Peer> entry = myPeers.floorEntry(randomNumber);

		if(entry != null) return entry.getValue();

		entry = myPeers.firstEntry();
		return (entry != null) ? entry.getValue() : null;
	}

	public void heartbeat() {
		for(Entry<Integer, Peer> entry: myPeers.entrySet()) {
			int peerID = entry.getKey();
			Peer peer = entry.getValue();

			try {
				peer.ping();
			}
			catch(RemoteException exception) {
				System.err.println("Peer with ID " + peerID + " might be down.");
			}
		}	
	}

	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.getRegistry();

			ManagerImplementation manager = new ManagerImplementation(registry);
			Manager managerStub = (Manager) UnicastRemoteObject.exportObject(manager, 0);

			registry.rebind("DynamoClone", managerStub);
			
			PeriodicAgent agent = new PeriodicAgent(manager);
			agent.start();
		}
		catch(Exception exception) {
			System.err.print("Failed to bind manager to registry: ");
			exception.printStackTrace();
		}
	}
}