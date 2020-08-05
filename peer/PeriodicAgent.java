package peer;

public class PeriodicAgent extends Thread {
	private PeerImplementation peer;

	public PeriodicAgent(PeerImplementation peer) {
		this.peer = peer;
	}

	public void run() {
		while(true) {
			//peer.updateMembers();
			peer.updateData();
			try {
				Thread.sleep(5000);
			}
			catch (InterruptedException exception) {
				// No problem
			}
		}
	}
}
