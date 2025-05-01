/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import Blockchain.Block;
import Blockchain.Blockchain;
import Blockchain.Localchain;
import Blockchain.Transaction;
import Blockchain.Wallet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import movement.MovementModel;
import movement.Path;
import routing.MessageRouter;
import routing.RoutingInfo;

/**
 * A DTN capable host.
 */
public class DTNHost implements Comparable<DTNHost> {

    private static int nextAddress = 0;
    private int address;

    private Coord location; 	// where is the host
    private Coord destination;	// where is it going

    private MessageRouter router;
    private MovementModel movement;
    private Path path;
    private double speed;
    private double nextTimeToMove;
    private String name;
    private List<MessageListener> msgListeners;
    private List<MovementListener> movListeners;
    private List<NetworkInterface> net;
    private ModuleCommunicationBus comBus;
    
    /**
     * Penanda untuk Collector, Home, dan Operator Proxy 
     * bahwa proses appending telah siap dan lanjut
     * proses selanjutnya yaitu rewarding ke miner.
     */
    private boolean appendingDone = false;
    
    /* -------------------------- FIELD MINER ------------------------------- */
        /**
         * Wallet milik node, setiap miner punya dompet digital
         */
        private Wallet wallet;
    /* ---------------------------------------------------------------------- */
    
        
        
    /* ----------------------- FIELD OPERATOR PROXY ------------------------- */
        /**
         * Penyimpanan sementara transaksi saat fase pembangkitan transaksi
         * oleh miner. Buffer ini yang nantinya akan dikelompokkan lagi menjadi
         * beberapa grup (list) lagi.
         */
        private List<Transaction> transactionBuffer;
        
        /**
         * Merupakan kumpulan dari list-list yang berasal dari transactionBuffer
         * tadi yang sudah dikelompokkan. List ini nanti yang akan dipilih
         * para penambang sesuai fee nya.
         */
        private List<List<Transaction>> trx;
        
        /**
         * Catatan milik OperatorProxy yang mencatat siapa saja miner yang
         * sudah dikunjungi selama proses mining maupun verifikasi. Pencatatan
         * diperlukan untuk mengantisipasi 1 miner melakukan mining berulang-
         * ulang karena bertemu OperatorProxy lebih dari sekali.
         */
        private Set<DTNHost> visitedMiner;
        
        /**
         * Blok tertambang yang terpilih setelah diverifikasi sebanyak "v"
         * miner.
         */
        private Block selectedBlock;
        
        /**
         * Banyaknya miner yang setuju bahwa Blok yang ditambang valid
         */
        private int v;
        
        /**
         * Localchain, setiap Operator Proxy punya masing-masing satu
         */
        private Localchain localchain;
        
        /**
         * Penanda bahwa Operator Proxy siap untuk melakukan storing setelah 
         * selesai proses mining-verifikasi
         */
        private boolean readyToStore;
        
        /**
         * Ukuran minimal paket di dalam list trx
         */
        private static final int MIN_PACKET_SIZE = 5;
        
        /**
         * Ukuran maksimal paket di dalam list trx
         */
        private static final int MAX_PACKET_SIZE = 8;
        
        /**
         * Library untuk menghasilkan angka acak untuk ukuran paket
         */
        private Random random;
        
        /**
         * Penanda bahwa Operator Proxy sudah melakukan grouping transaction.
         * Jadi trx sudah berisi list yang berisi list-list transaksi yang
         * telah digrup.
         */
        private boolean hasGrouped;
        
        /**
         * Catatan OperatorProxy untuk menyimpan siapa saja miner di area
         * mereka yang melakukan kontribusi mining untuk diberikan fee.
         */
        private Set<DTNHost> rewardedMiner;
        
        /**
         * Catatan OperatorProxy saat sudah selesai memberikan reward. Jika
         * bernilai true maka tandanya proses sudah selesai
         */
        private boolean doneReward;
    /* ---------------------------------------------------------------------- */

        
        
    /* --------------------------- FIELD HOME ------------------------------- */
        /**
         * Kumpulan Localchain-localchain yang sudah distore
         * oleh para Operator Proxy.
         */
        private List<Localchain> storedLocalchains;
        
        /**
         * Catatan untuk home menyimpan siapa saja OperatorProxy yang sudah
         * berkunjung ke Home
         */
        private Set<DTNHost> visitedOperatorProxy;
        
        /**
         * Catatan untuk Home mencatat tiap Operator Proxy yang sudah selesai
         * memberikan reward pada miner-miner di areanya.
         */
        private Set<DTNHost> confirmedDoneOperatorProxy;
    /* ---------------------------------------------------------------------- */
    
        
        
    /* ----------------------- FIELD COLLECTOR ------------------------------ */
        /**
         * Localchain terpilih yang menurut Collector adalah localchain
         * dengan panjang rantai terbesar.
         */
        private Localchain selectedLocalchain;
    /* ---------------------------------------------------------------------- */
    
        
        
     /* ----------------------- FIELD INTERNET ----------------------- */
        /**
         * Blockchain utama yang berada di Internet.
         */
        private Blockchain mainChain;
    /* --------------------------------------------------------------- */


    static {
        DTNSim.registerForReset(DTNHost.class.getCanonicalName());
        reset();
    }

    /**
     * Creates a new DTNHost.
     *
     * @param msgLs Message listeners
     * @param movLs Movement listeners
     * @param groupId GroupID of this host
     * @param interf List of NetworkInterfaces for the class
     * @param comBus Module communication bus object
     * @param mmProto Prototype of the movement model of this host
     * @param mRouterProto Prototype of the message router of this host
     */
    public DTNHost(List<MessageListener> msgLs,
            List<MovementListener> movLs,
            String groupId, List<NetworkInterface> interf,
            ModuleCommunicationBus comBus,
            MovementModel mmProto, MessageRouter mRouterProto) {
        this.comBus = comBus;
        this.location = new Coord(0, 0);
        this.address = getNextAddress();
        this.name = groupId + address;
        this.net = new ArrayList<>();

        for (NetworkInterface i : interf) {
            NetworkInterface ni = i.replicate();
            ni.setHost(this);
            net.add(ni);
        }
        
        /* Inisialisasi untuk Operator Proxy */
        if (this.name.startsWith("ope")) {
            this.trx = new ArrayList<>();
            this.transactionBuffer = new ArrayList<>();
            this.visitedMiner = new HashSet<>();
            this.v = 0;
            this.random = new Random();
            this.hasGrouped = false;
            this.rewardedMiner = new HashSet<>();
            this.doneReward = false;
        }
        
        /* Inisialisasi untuk Home */
        if (this.name.startsWith("hom")) {
            this.storedLocalchains = new ArrayList<>();
            this.visitedOperatorProxy = new HashSet<>();
            this.readyToStore = false;
            this.confirmedDoneOperatorProxy = new HashSet<>();
        }
        
        /* Inisialisasi untuk Miner */
        if (this.name.startsWith("min")) {
            this.wallet = new Wallet();
        }

        // TODO - think about the names of the interfaces and the nodes
        // this.name = groupId + ((NetworkInterface)net.get(1)).getAddress();
        this.msgListeners = msgLs;
        this.movListeners = movLs;

        // create instances by replicating the prototypes
        this.movement = mmProto.replicate();
        this.movement.setComBus(comBus);
        setRouter(mRouterProto.replicate());

        this.location = movement.getInitialLocation();

        this.nextTimeToMove = movement.nextPathAvailable();
        this.path = null;

        if (movLs != null) { // inform movement listeners about the location
            for (MovementListener l : movLs) {
                l.initialLocation(this, this.location);
            }
        }
    }


    /**
     * Menambahkan transaksi yang telah dibangkitkan miner ke buffer milik
     * Operator Proxy untuk diproses nanti. Saat sudah mecapai periode tertentu,
     * semua transaksi akan dibungkus menjadi kelompok-kelompok terpisah.
     * @param trx Transaksi yang dibuat oleh miner.
     */
    public void addTransactionToBuffer(Transaction trx) {
        if (hasGrouped) {
            return;
        }
        
        transactionBuffer.add(trx);
    }
    
    /**
     * Mengelompokkan transaksi 
     */
    public void groupTransactions() {
        if (!hasGrouped) {
            int jumlah = transactionBuffer.size();
            while (!transactionBuffer.isEmpty()) {
                // Tentukan ukuran paket secara acak
                int packetSize = random.nextInt(MAX_PACKET_SIZE - MIN_PACKET_SIZE + 1) + MIN_PACKET_SIZE;
                
                packetSize = Math.min(packetSize, transactionBuffer.size()); // Pastikan tidak melebihi jumlah transaksi yang ada

                // Buat paket transaksi
                List<Transaction> packet = new ArrayList<>();
                for (int i = 0; i < packetSize; i++) {
                    packet.add(transactionBuffer.remove(0)); // Ambil transaksi dari buffer
                }

                // Tambahkan paket ke daftar paket transaksi
                trx.add(packet);
            }

            System.out.println("Sebanyak "+jumlah+" transaksi tersimpan di buffer!");
            System.out.println("Semua transaksi telah dikelompokkan di " + name);
            this.hasGrouped = true;
        }
    }
    
    /**
     * Penanda bahwa Operator Proxy siap melakukan storing Localchain ke Home.
     * @return true jika list trx Operator Proxy empty.
     */
    public boolean isReadyToStore() {return readyToStore;}

    /**
     * Penanda bahwa Operator Proxy siap membagikan fee ke para miner yang
     * berhasil berkontribusi dalam proses mining.
     * @return true jika telah bertemu Home dan sudah diinfokan dari Collector
     * bahwa appending telah selesai.
     */
    public boolean isAppendingDone() {return appendingDone;}

    /**
     * Penannda bahwa Operator Proxy sudah selesai membagikan reward kepada
     * miner yang sudah berkontribusi dalam proses mining di areanya.
     * @return true jika data list block-block yang dipegang Operator Proxy
     * sudah habis.
     */
    public boolean isDoneReward() {return doneReward;}

    public void setDoneReward(boolean doneReward) {this.doneReward = doneReward;}

    public void setReadyToStore(boolean readyToStore) {this.readyToStore = readyToStore;}
    
    public void setAppendingDone(boolean appendingDone) {this.appendingDone = appendingDone;}

    public List<Transaction> getTransactionBuffer() {return transactionBuffer;}

    public void setTransactionBuffer(List<Transaction> transactionBuffer) {this.transactionBuffer = transactionBuffer;}
    
    public Blockchain getMainChain() {return mainChain;}

    public void setMainChain(Blockchain mainChain) {this.mainChain = mainChain;}
    
    public Localchain getSelectedLocalchain() {return selectedLocalchain;}

    public void setSelectedLocalchain(Localchain selectedLocalchain) {this.selectedLocalchain = selectedLocalchain;}

    public Set<DTNHost> getRewardedMiners() { return rewardedMiner;}

    public void setRewardedMiner(Set<DTNHost> rewardedMiner) {this.rewardedMiner = rewardedMiner;}
    
    public Wallet getWallet() {return wallet;}

    public List<Localchain> getStoredLocalchains() {return storedLocalchains;}

    public Set<DTNHost> getVisitedOperatorProxy() {return visitedOperatorProxy;}

    public int getV() {return v;}

    public void setV(int v) {this.v = v;}

    public Block getSelectedBlock() {return selectedBlock;}

    public void setSelectedBlock(Block selectedBlock) {this.selectedBlock = selectedBlock;}

    public List<List<Transaction>> getTrx() {return trx;}

    public void setTrx(List<List<Transaction>> trx) {this.trx = trx;}

    public Localchain getLocalchain() {return localchain;}

    public void setLocalchain(Localchain localchain) {this.localchain = localchain;}

    public Set<DTNHost> getVisitedMiner() {return visitedMiner;}

    public void setVisitedMiner(Set <DTNHost> visitedMiner) {this.visitedMiner = visitedMiner;}

    public Set<DTNHost> getConfirmedDoneOperatorProxy() {return confirmedDoneOperatorProxy;}

    public void setConfirmedDoneOperatorProxy(Set<DTNHost> confirmedDoneOperatorProxy) {this.confirmedDoneOperatorProxy = confirmedDoneOperatorProxy;}

    /**
     * Returns a new network interface address and increments the address for
     * subsequent calls.
     *
     * @return The next address.
     */
    private synchronized static int getNextAddress() {
        return nextAddress++;
    }

    /**
     * Reset the host and its interfaces
     */
    public static void reset() {
        nextAddress = 0;
    }

    /**
     * Returns true if this node is active (false if not)
     *
     * @return true if this node is active (false if not)
     */
    public boolean isActive() {
        return this.movement.isActive();
    }

    /**
     * Set a router for this host
     *
     * @param router The router to set
     */
    private void setRouter(MessageRouter router) {
        router.init(this, msgListeners);
        this.router = router;
    }

    /**
     * Returns the router of this host
     *
     * @return the router of this host
     */
    public MessageRouter getRouter() {
        return this.router;
    }

    /**
     * Returns the network-layer address of this host.
     * @return 
     */
    public int getAddress() {
        return this.address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Returns this hosts's ModuleCommunicationBus
     *
     * @return this hosts's ModuleCommunicationBus
     */
    public ModuleCommunicationBus getComBus() {
        return this.comBus;
    }

    /**
     * Informs the router of this host about state change in a connection
     * object.
     *
     * @param con The connection object whose state changed
     */
    public void connectionUp(Connection con) {
        this.router.changedConnection(con);
    }

    public void connectionDown(Connection con) {
        this.router.changedConnection(con);
    }

    /**
     * Returns a copy of the list of connections this host has with other hosts
     *
     * @return a copy of the list of connections this host has with other hosts
     */
    public List<Connection> getConnections() {
        List<Connection> lc = new ArrayList<>();

        for (NetworkInterface i : net) {
            lc.addAll(i.getConnections());
        }

        return lc;
    }

    /**
     * Returns the current location of this host.
     *
     * @return The location
     */
    public Coord getLocation() {
        return this.location;
    }

    /**
     * Returns the Path this node is currently traveling or null if no path is
     * in use at the moment.
     *
     * @return The path this node is traveling
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Sets the Node's location overriding any location set by movement model
     *
     * @param location The location to set
     */
    public void setLocation(Coord location) {
        this.location = location.clone();
    }

    /**
     * Sets the Node's name overriding the default name (groupId + netAddress)
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the messages in a collection.
     *
     * @return Messages in a collection
     */
    public Collection<Message> getMessageCollection() {
        return this.router.getMessageCollection();
    }

    /**
     * Returns the number of messages this node is carrying.
     *
     * @return How many messages the node is carrying currently.
     */
    public int getNrofMessages() {
        return this.router.getNrofMessages();
    }

    /**
     * Returns the buffer occupancy percentage. Occupancy is 0 for empty buffer
     * but can be over 100 if a created message is bigger than buffer space that
     * could be freed.
     *
     * @return Buffer occupancy percentage
     */
    public double getBufferOccupancy() {
        double bSize = router.getBufferSize();
        double freeBuffer = router.getFreeBufferSize();
        return 100 * ((bSize - freeBuffer) / bSize);
    }

    /**
     * Returns routing info of this host's router.
     *
     * @return The routing info.
     */
    public RoutingInfo getRoutingInfo() {
        return this.router.getRoutingInfo();
    }

    /**
     * Returns the interface objects of the node
     * @return List interface
     */
    public List<NetworkInterface> getInterfaces() {
        return net;
    }

    /**
     * Find the network interface based on the index
     * @param interfaceNo
     * @return 
     */
    protected NetworkInterface getInterface(int interfaceNo) {
        NetworkInterface ni = null;
        try {
            ni = net.get(interfaceNo - 1);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("No such interface: " + interfaceNo);
            System.exit(0);
        }
        return ni;
    }

    /**
     * Find the network interface based on the interfacetype
     * @param interfacetype
     * @return 
     */
    protected NetworkInterface getInterface(String interfacetype) {
        for (NetworkInterface ni : net) {
            if (ni.getInterfaceType().equals(interfacetype)) {
                return ni;
            }
        }
        return null;
    }

    /**
     * Force a connection event
     * @param anotherHost
     * @param interfaceId
     * @param up
     */
    public void forceConnection(DTNHost anotherHost, String interfaceId,
            boolean up) {
        NetworkInterface ni;
        NetworkInterface no;

        if (interfaceId != null) {
            ni = getInterface(interfaceId);
            no = anotherHost.getInterface(interfaceId);

            assert (ni != null) : "Tried to use a nonexisting interfacetype " + interfaceId;
            assert (no != null) : "Tried to use a nonexisting interfacetype " + interfaceId;
        } else {
            ni = getInterface(1);
            no = anotherHost.getInterface(1);

            assert (ni.getInterfaceType().equals(no.getInterfaceType())) :
                    "Interface types do not match.  Please specify interface type explicitly";
        }

        if (up) {
            ni.createConnection(no);
        } else {
            ni.destroyConnection(no);
        }
    }

    /**
     * for tests only --- do not use!!!
     * @param h
     */
    public void connect(DTNHost h) {
        System.err.println(
                "WARNING: using deprecated DTNHost.connect(DTNHost)"
                + "\n Use DTNHost.forceConnection(DTNHost,null,true) instead");
        forceConnection(h, null, true);
    }

    /**
     * Updates node's network layer and router.
     *
     * @param simulateConnections Should network layer be updated too
     */
    public void update(boolean simulateConnections) {
        if (!isActive()) {
            return;
        }

        if (simulateConnections) {
            for (NetworkInterface i : net) {
                i.update();
            }
        }
        this.router.update();
    }

    /**
     * Moves the node towards the next waypoint or waits if it is not time to
     * move yet
     *
     * @param timeIncrement How long time the node moves
     */
    public void move(double timeIncrement) {
        double possibleMovement;
        double distance;
        double dx, dy;

        if (!isActive() || SimClock.getTime() < this.nextTimeToMove) {
            return;
        }
        if (this.destination == null) {
            if (!setNextWaypoint()) {
                return;
            }
        }

        possibleMovement = timeIncrement * speed;
        distance = this.location.distance(this.destination);

        while (possibleMovement >= distance) {
            // node can move past its next destination
            this.location.setLocation(this.destination); // snap to destination
            possibleMovement -= distance;
            if (!setNextWaypoint()) { // get a new waypoint
                return; // no more waypoints left
            }
            distance = this.location.distance(this.destination);
        }

        // move towards the point for possibleMovement amount
        dx = (possibleMovement / distance) * (this.destination.getX()
                - this.location.getX());
        dy = (possibleMovement / distance) * (this.destination.getY()
                - this.location.getY());
        this.location.translate(dx, dy);
    }

    /**
     * Sets the next destination and speed to correspond the next waypoint on
     * the path.
     *
     * @return True if there was a next waypoint to set, false if node still
     * should wait
     */
    private boolean setNextWaypoint() {
        if (path == null) {
            path = movement.getPath();
        }

        if (path == null || !path.hasNext()) {
            this.nextTimeToMove = movement.nextPathAvailable();
            this.path = null;
            return false;
        }

        this.destination = path.getNextWaypoint();
        this.speed = path.getSpeed();

        if (this.movListeners != null) {
            for (MovementListener l : this.movListeners) {
                l.newDestination(this, this.destination, this.speed);
            }
        }

        return true;
    }

    /**
     * Sends a message from this host to another host
     *
     * @param id Identifier of the message
     * @param to Host the message should be sent to
     */
    public void sendMessage(String id, DTNHost to) {
        this.router.sendMessage(id, to);
    }

    /**
     * Start receiving a message from another host
     *
     * @param m The message
     * @param from Who the message is from
     * @return The value returned by
     * {@link MessageRouter#receiveMessage(Message, DTNHost)}
     */
    public int receiveMessage(Message m, DTNHost from) {
        int retVal = this.router.receiveMessage(m, from);

        if (retVal == MessageRouter.RCV_OK) {
            m.addNodeOnPath(this);	// add this node on the messages path
        }

        return retVal;
    }

    /**
     * Requests for deliverable message from this host to be sent trough a
     * connection.
     *
     * @param con The connection to send the messages trough
     * @return True if this host started a transfer, false if not
     */
    public boolean requestDeliverableMessages(Connection con) {
        return this.router.requestDeliverableMessages(con);
    }

    /**
     * Informs the host that a message was successfully transferred.
     *
     * @param id Identifier of the message
     * @param from From who the message was from
     */
    public void messageTransferred(String id, DTNHost from) {
        this.router.messageTransferred(id, from);
    }

    /**
     * Informs the host that a message transfer was aborted.
     *
     * @param id Identifier of the message
     * @param from From who the message was from
     * @param bytesRemaining Nrof bytes that were left before the transfer would
     * have been ready; or -1 if the number of bytes is not known
     */
    public void messageAborted(String id, DTNHost from, int bytesRemaining) {
        this.router.messageAborted(id, from, bytesRemaining);
    }

    /**
     * Creates a new message to this host's router
     *
     * @param m The message to create
     */
    public void createNewMessage(Message m) {
        this.router.createNewMessage(m);
    }

    /**
     * Deletes a message from this host
     *
     * @param id Identifier of the message
     * @param drop True if the message is deleted because of "dropping" (e.g.
     * buffer is full) or false if it was deleted for some other reason (e.g.
     * the message got delivered to final destination). This effects the way the
     * removing is reported to the message listeners.
     */
    public void deleteMessage(String id, boolean drop) {
        this.router.deleteMessage(id, drop);
    }

    /**
     * Returns a string presentation of the host.
     *
     * @return Host's name
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Checks if a host is the same as this host by comparing the object
     * reference
     *
     * @param otherHost The other host
     * @return True if the hosts objects are the same object
     */
    public boolean equals(DTNHost otherHost) {
        return this == otherHost;
    }

    /**
     * Compares two DTNHosts by their addresses.
     *
     * @param h
     * @return 
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(DTNHost h) {
        return this.getAddress() - h.getAddress();
    }

}
