package client;

import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.server.UnicastRemoteObject;

import manager.Manager;
import peer.Peer;

import java.util.Scanner;

public class ClientImplementation implements Client {
	public ClientImplementation() {
		super();
	}

	public void submitAnswerGet(Integer key, String value) throws RemoteException {
		System.out.println(key + " -> " + value);
	}

	public void submitAnswerPut(Integer key, String previousValue) throws RemoteException {
		System.out.println(key + " " + (previousValue != null ? "replaced" : "inserted"));
	}

	private static int promptKey(Scanner input) {
		System.out.print("Type key: ");

		String line = input.nextLine();

		try {
			return Integer.valueOf(line);
		}
		catch(NumberFormatException exception) {
			return 0;
		}
	}

	private static String promptValue(Scanner input) {
		System.out.print("Type value: ");

		return input.nextLine();
	}

	public static void main(String args[]) {
		Manager manager = null;

		try {
			Registry registry = LocateRegistry.getRegistry();

			manager = (Manager) registry.lookup("DynamoClone");
		}
		catch(Exception exception) {
			System.err.print("Failed to locate server in the registry: ");
			exception.printStackTrace();

			return;
		}

		Client client = new ClientImplementation();
		Client clientStub = null;

		try {
			clientStub = (Client) UnicastRemoteObject.exportObject(client, 0);
		}
		catch(RemoteException exception) {
			System.err.print("Failed to export client: ");
			exception.printStackTrace();

			return;
		}

		Scanner input = new Scanner(System.in);

		try {
			do {
				System.out.print("Say \"put\", \"get\", or \"exit\": ");

				String operation = input.nextLine();
				
				Peer randPeer = manager.getRandom();

				if(operation.equals("put")) {
					int key = promptKey(input);
					String value = promptValue(input);

					// The answer will be notified back to us via submitAnswerPut
					randPeer.put(key, value, clientStub);
				}
				else if(operation.equals("get")) {
					int key = promptKey(input);

					// The answer will be notified back to us via submitAnswerGet
					randPeer.get(key, clientStub);
				}
				else if(operation.equals("exit")) {
					System.out.println("Exiting");

					break;
				}
			} while(true);
		}
		catch(RemoteException exception) {
			System.err.print("Failed to contact manager: ");
			exception.printStackTrace();

			return;
		}

		input.close();
	}
}