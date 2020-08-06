# Amazon-Dynamo Clone
This is a simple implementation of Dynamo, Amazon's highly available key-value storage system. Dynamo utilizes a combination of techniques to achieve scalability and availability.

# Main Components
+ Manager Server
  - Works as a central master node by distributing put() and get() requests to peer servers
  - Interacts with the Java RMI registry on behalf of peer servers
+ Peer Server
  - Holds keys for which it is the authoritative peer
  - Performs put() and get() requests on behalf of the client, or
  - Forwards put() and get() requests to key k's authoritative peer
+ Client 
  - Contacts the manager for its put() and get() requests
  
# Techniques/Ideas Used
+ Consistent Hashing
  - Partitions keys and peer servers in a circular array of authoritative peers
  - Allows minimal reallocation of keys when peer servers enter the system
+ Distributed Membership
  - Joining peers are responsible for updating member lists in other peers
+ Data replication
  - Each key is replicated in the two directly clockwise peer server to the authoritative server

# How To Run
1. Run *./stop_compile.sh* to compile all files
2. Run *rmiregistry* in the directory in which this project resides, 
and wait a few moments for the rmiregistry command to process
4. Run *java manager.ManagerImplementation* in a new terminal tab
5. Run *java peer.PeerImplementation* in a new terminal tab
6. Repeat step 5 until you are content with the number of peers
7. Run *java client.clientImplementation* in a new terminal tab
8. Follow the prompts in the client's tab to get() and put() information

# Next Steps
Learn what a Merkle tree is! Implement it here!
