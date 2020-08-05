echo "Stopping all previous instances of manager, peers, clients..."
ps | grep ManagerImplementation | grep -v grep | cut -f 1 -d ' ' | xargs kill
ps | grep PeerImplementation | grep -v grep | cut -f 1 -d ' ' | xargs kill
ps | grep ClientImplementation | grep -v grep | cut -f 1 -d ' ' | xargs kill
killall rmiregistry
echo "OK"

echo "Compiling manager, peers, client..."
javac manager/ManagerImplementation.java
javac peer/PeerImplementation.java
javac client/ClientImplementation.java
echo "OK"
echo ""
echo ""

echo "From this directory, run:"
echo "  rmiregistry" 
echo "Open new tab, run your manager (master):" 
echo "  java manager.ManagerImplementation" 
echo "Open new tab, run your peer (worker):" 
echo "  java peer.PeerImplementation"
echo "..."
echo "(run as many peers as you'd like)"
echo "..."
echo "Open new tab, run your client:"
echo "  java client.ClientImplementation" 