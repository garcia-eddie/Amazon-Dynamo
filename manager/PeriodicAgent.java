package manager;

public class PeriodicAgent extends Thread {
	private ManagerImplementation manager;

	public PeriodicAgent(ManagerImplementation manager) {
		this.manager = manager;
	}

	public void run() {
		while(true) {
			manager.heartbeat();
			try {
				Thread.sleep(5000);
			}
			catch (InterruptedException exception) {
				// No problem
			}
		}
	}
}
