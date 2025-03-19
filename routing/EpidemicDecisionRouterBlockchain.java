package routing;

import Blockchain.Block;
import Blockchain.Localchain;
import Blockchain.Transaction;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EpidemicDecisionRouterBlockchain implements RoutingDecisionEngine {

    /**
     * For Report purpose, maybe needed some variable
     */
    protected LinkedList<Double> resourcesList;
    public static final String TOTAL_CONTACT_INTERVAL = "perTotalContact";
    public static final int DEFAULT_CONTACT_INTERVAL = 300;
    private static final String THRESHOLD = "threshold";
    private static final int DEFAULT_THRESHOLD = 6;
    private Double lastRecord = Double.MIN_VALUE;
    private int interval;
    private List<Block> minedBlock;
    private int threshold;

    public EpidemicDecisionRouterBlockchain(Settings s) {
        minedBlock = new ArrayList<>();
        if (s.contains(TOTAL_CONTACT_INTERVAL)) {
            interval = s.getInt(TOTAL_CONTACT_INTERVAL);
        } else {
            interval = DEFAULT_CONTACT_INTERVAL;
        }

        if (s.contains(THRESHOLD)) {
            threshold = s.getInt(THRESHOLD);
        } else {
            threshold = DEFAULT_THRESHOLD;
        }
    }

    public EpidemicDecisionRouterBlockchain(EpidemicDecisionRouterBlockchain proto) {
        minedBlock = new ArrayList<>();
        resourcesList = new LinkedList<>();
        interval = proto.interval;
        lastRecord = proto.lastRecord;
        this.threshold = proto.threshold;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost host = con.getOtherNode(peer);

        /*
        Ada waktu 20000 ms untuk melakukan inisialisasi awal terlebih dahulu.
        Di sini kami berasumsi bahwa 10000 ms pertama dilakukan para miner
        untuk melakukan/membangkitkan transaksi. Transaksi akan dibungkus
        ke dalam message, dan destinasi hanya akan ke Operator Proxy dari setiap
        area. Lalu setelah 10000 ms berikutnya, maka transaksi-transaksi ini 
        akan dikumpulkan dan dibuat grup untuk nanti dilakukan proses mining 
        oleh para miner
         */
        if (SimClock.getTime() >= 20000) {

            /*
                Mining dilakukan oleh Operator proxy dan Miner dengan cara
                membagikan list transaksi berisikian transaksi-transaksi yang
                sudah dibuat miner tadi ke para miner di area tersebut. Lalu para
                miner akan membungkus ke dalam satu blok dengan nonce masih 0,
                dan pada proses ini miner-miner akan mencari nilai nonce sehingga
                hash dapat mencapai tingkut difficulty yang sudah diatur, masing-
                masing miner akan dicatat waktu durasi mining mereka, tujuannya
                untuk memilih blok terbaik dengan interval waktu mining tercepat, 
                lalu akan disimpan ke dalam selectedBlock. Saat selectedBlock
                terisi, proses selanjutnya yaitu memverifikasi blok.
             */
            mining_algorithmOne(host, peer);

            /*
                Setelah memilih blok terbaik, operator proxy kembali membagikan
                hasil blok terbaik tersebut kepada miner untuk diverifikasi, hasil
                verifikasi beriringan dengan bertambahnya nilai v, jika nilai v
                sudah memenuhi threshold, blok dianggap valid dan ditambahkan ke
                dalam chain lokal yaitu localChain yang dimiliki oleh para Operator
                Proxy. Proses mining dan verification berlangsung berurutan, jika
                verifikasi selesai namun masih ada list transaksi di Operator Proxy
                yang belum diurus, maka kembali lagi ke proses mining. Proses selan-
                jutnya akan dilakukan saat list transaksi pada Operator Proxy sudah 
                tidak ada lagi.
             */
            verification_algorithmTwo(host, peer);

            /*
                Setelah list transaksi sudah habis, dan proses verifikasi sudah
                dilakukan dan ditambahkan ke localchain milik masing-masing 
                Operator Proxy, operator proxy menyetor localchainnya ke home saat
                mereka bertemu, perlu diingat Operator Proxy memiliki field
                "readyToStore" untuk menandakan sudah tidak ada list transaksi
                tersisa untuk diproses, jika bernilai true, localchain dari masing-
                masing Operator Proxy akan disetor ke Home (storedLocalchain).
             */
            storing_algorithmThree(host, peer);


            /* 
                Setelah localchain dikumpulkan, Home akan terlebih dahulu
                menghitung hash dari masing-masing localchain, lalu selanjutnya
                diserahkan kepada Collector. Pada proses ini, Collector memilah
                localchain mana yang sebaiknya ditambahkan terlebih dahulu ke
                Blockchain nanti, dengan memilih localchain dengan size terbesar
                yang akan ditambahkan duluan
             */
            selection_algorithmFour(host, peer);

            /*
                Setelah localchain terbaik dipilih, maka localchain akan
                ditambahkan ke main chain di Internet. Blok-blok pada localchain
                akan dipecah, dan akan diset previous hash mereka sehingga
                harus kembali dilakukan recalculate hash. Hasilnya akan diappend
                ke blockchain utama
             */
            appending_algorithmFive(host, peer);

        }
    }

    /**
     * Implements the mining algorithm where an Operator Proxy selects the best
     * transaction list, assigns it to a miner, and records the mining time. The
     * best-mined block is then selected and stored in the local block chain.
     *
     * @param host The DTNHost that acts as an Operator Proxy and manages the
     * mining process.
     * @param peer The DTNHost that acts as a Miner and performs the mining
     * operation.
     */
    private void mining_algorithmOne(DTNHost host, DTNHost peer) {
        if (isOperatorProxy(host)) {

            if (host.getSelectedBlock() == null && !host.getTrx().isEmpty()) {

                List<List<Transaction>> trx = host.getTrx();
                Localchain localChain = host.getLocalchain();
                String previousHash = localChain.getLatestBlock().getHash();

                if (isMiner(peer)) {

                    if (!host.getVisitedMiner().contains(peer)) { // jika baru pertama kali bertemu

                        host.getVisitedMiner().add(peer);

                        int indexBestTRX = getBestTranx(trx);
                        List<Transaction> bestTransactionList = new ArrayList<>(trx.get(indexBestTRX));

                        Block b = new Block(previousHash, bestTransactionList, System.currentTimeMillis());
                        b.setFee(getFee(bestTransactionList));
                        b.setMinedBy(peer);

                        long begin = System.currentTimeMillis();

                        b.mineBlock(localChain.getDifficulty());

                        long end = System.currentTimeMillis();
                        long time = end - begin;

                        b.setIntervalMining(time);

                        minedBlock.add(b);
                    }
                }

                if (host.getVisitedMiner().size() == 7) {

                    host.getVisitedMiner().clear();

                    int indexBestTRX = getBestTranx(trx);
                    host.getTrx().remove(indexBestTRX);

                    int index = getBestMinedBlock(minedBlock);

                    Block selectedBlock = new Block(minedBlock.get(index));
                    host.setSelectedBlock(selectedBlock);

                    minedBlock.clear();
                }
            }
        }
    }

    /**
     * Implements the verification algorithm where an Operator Proxy shares the
     * best-mined block with other miners for verification. The block is
     * considered valid if it receives enough verification votes (based on the
     * threshold). Once the block is verified, it is added to the local
     * blockchain, and the Operator Proxy is marked as ready to store the
     * localchain if there are no remaining transactions to process.
     *
     * @param host The DTNHost that acts as an Operator Proxy and manages the
     * verification process.
     * @param peer The DTNHost that acts as a Miner and performs the
     * verification operation.
     */
    private void verification_algorithmTwo(DTNHost host, DTNHost peer) {
        if (isOperatorProxy(host)) {
            if (host.getSelectedBlock() != null) {

                Localchain localChain = host.getLocalchain();
                Block selectedBlock = host.getSelectedBlock();

                if (isMiner(peer)) {

                    if (!host.getVisitedMiner().contains(peer)) {// jika baru pertama kali bertemu

                        host.getVisitedMiner().add(peer);

                        String targetHash = selectedBlock.calculateHash();

                        Block b = new Block(selectedBlock);
                        String hash = b.calculateHash();

                        if (targetHash.equals(hash)) {
                            host.setV(host.getV() + 1);
                        }
                    }

                    if (host.getV() == threshold) {
                        if (!(host.getV() > threshold)) {

                            //tambahkan selectedBlock ke dalam localchain
                            localChain.addBlock(new Block(selectedBlock));

                            //reset v
                            host.setV(0);

                            //reset visitedMiner
                            host.getVisitedMiner().clear();

                            // reset selectedBlock
                            host.setSelectedBlock(null);

                            if (host.getTrx().isEmpty() && host.isReadyToStore() == false) {
                                host.setReadyToStore(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Implements the storing algorithm where an Operator Proxy stores its local
     * blockchain to the Home node if it is ready to store. The Home node keeps
     * track of the stored localchains and ensures that each Operator Proxy only
     * stores its localchain once.
     *
     * @param host The DTNHost that acts as an Operator Proxy and is ready to
     * store its localchain.
     * @param peer The DTNHost that acts as a Home node and receives the
     * localchain for storage.
     */
    private void storing_algorithmThree(DTNHost host, DTNHost peer) {
        if (isOperatorProxy(host)) {
            if (isHome(peer)) {
                if (!peer.getVisitedOperatorProxy().contains(host)) {
                    if (host.isReadyToStore()) {
                        peer.getVisitedOperatorProxy().add(host);
                        peer.getStoredLocalchains().add(host.getLocalchain());
                        System.out.println("Localchain " + host + " stored!, "
                                + "Storage size: " + peer.getStoredLocalchains().size());
                    }
                }
            }
        }
    }

    /**
     * Implements the selection algorithm where a Collector selects the best
     * local chain from the Home node based on the chain size. The selected
     * localchain is then prepared for uploading to the internet. If all
     * transactions have been added to the blockchain, the simulation is
     * terminated.
     *
     * @param host The DTNHost that acts as a Home node and stores the
     * localchains.
     * @param peer The DTNHost that acts as a Collector and selects the best
     * localchain.
     */
    private void selection_algorithmFour(DTNHost host, DTNHost peer) {
        if (isHome(host)) {
            if (isCollector(peer)) {

                if (peer.getSelectedLocalchain() == null) {

                    if (host.getStoredLocalchains().size() == SimScenario.getInstance().localChainCount) {
                        if (host.getStoredLocalchains().isEmpty()) {
                            System.out.println("SEMUA TRANSAKSI SUDAH DITAMBAHKAN DI BLOCKCHAIN");
                            System.exit(0);
                        }

                        for (Localchain sL : host.getStoredLocalchains()) {
                            String hash = sL.calculateHash();
                            sL.setHash(hash);
                        }

                        Localchain selected = null;
                        for (Localchain lc : host.getStoredLocalchains()) {
                            if (selected == null || lc.chainSize() > selected.chainSize()) {
                                selected = lc;
                            }
                        }
                        peer.setSelectedLocalchain(selected);

                        host.getStoredLocalchains().remove(selected);

                        System.out.println("Localchain terbaik berhasil dipilih! "
                                + "Mengunggah ke internet......");
                    }
                }
            }
        }
    }

    /**
     * Implements the appending algorithm where a Collector uploads the selected
     * localchain to the Internet node for appending to the main blockchain. The
     * localchain is validated by comparing its hash before appending. If all
     * localchains have been processed, the main blockchain is printed.
     *
     * @param host The DTNHost that acts as a Collector and holds the selected
     * localchain.
     * @param peer The DTNHost that acts as an Internet node and appends the
     * localchain to the main blockchain.
     */
    private void appending_algorithmFive(DTNHost host, DTNHost peer) {
        if (isCollector(host)) {
            if (isInternet(peer)) {

                if (host.getSelectedLocalchain() != null) {
                    SimScenario.getInstance().localChainCount--;
                    String hash = host.getSelectedLocalchain().getHash();
                    String calculatedHash = host.getSelectedLocalchain().calculateHash();

                    if (calculatedHash.equals(hash)) {
                        peer.getMainChain().addBlockFromLocalChain(host.getSelectedLocalchain());

                        host.setSelectedLocalchain(null);
                        System.out.println("Transaksi berhasil ditambahkan ke Blockchain!");
                    }
                    if (SimScenario.getInstance().localChainCount == 0) {
                        System.out.println(peer.getMainChain());
                    }
                }
            }
        }
    }

    /**
     * Finds the index of the transaction list with the highest total amount.
     *
     * @param trx A list of transaction lists to be evaluated.
     * @return The index of the transaction list with the highest total amount.
     */
    private int getBestTranx(List<List<Transaction>> trx) {
        int index = -1;
        double maxTotal = 0;
        for (int i = 0; i < trx.size(); i++) {
            double tempTotal = 0;
            for (Transaction t : trx.get(i)) {
                tempTotal += t.getAmount();
            }

            if (tempTotal > maxTotal) {
                maxTotal = tempTotal;
                index = i;
            }
        }
        return index;
    }

    /**
     * Calculates the total transaction fee based on a percentage of the total
     * transaction amount.
     *
     * @param t A list of transactions whose total fee is to be calculated.
     * @return The calculated transaction fee, which is a percentage of the
     * total transaction amount.
     */
    private double getFee(List<Transaction> t) {
        double total = 0;
        for (Transaction tr : t) {
            total += tr.getAmount();
        }

        return 0.05 * total;
    }

    /**
     * Finds the index of the block with the shortest mining time.
     *
     * @param minedBlock A list of mined blocks to be evaluated.
     * @return The index of the block that was mined in the shortest time.
     */
    private int getBestMinedBlock(List<Block> minedBlock) {
        if (minedBlock.isEmpty()) {
            return -1; // Return -1 jika daftar kosong
        }

        int index = 0;
        long min = minedBlock.get(0).getIntervalMining();

        for (int i = 1; i < minedBlock.size(); i++) {
            if (minedBlock.get(i).getIntervalMining() < min) {
                min = minedBlock.get(i).getIntervalMining();
                index = i;
            }
        }
        return index;
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        if (isOperatorProxy(thisHost)) {
            Transaction trx = (Transaction) m.getProperty("transaction");
            if (trx != null) {
                addTransactionToBuffer(thisHost, trx);
            }
        }
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    private void addTransactionToBuffer(DTNHost host, Transaction trx) {
        host.addTransactionToBuffer(trx);
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (isMiner(thisHost) && isOperatorProxy(otherHost)) {
            return true;
        }
//        if (isMiner(thisHost) && isCollector(otherHost)) {
//            return false;
//        }
//        else if (isOperatorProxy(thisHost) && isCollector(otherHost)){
//            return false;
//        }
//        else if (isOperatorProxy(thisHost) && isHome(otherHost)){
//            return false;
//        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new EpidemicDecisionRouterBlockchain(this);
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    private boolean isOperatorProxy(DTNHost host) {
        return host.toString().startsWith("ope");
    }

    private boolean isMiner(DTNHost host) {
        return host.toString().startsWith("min");
    }

    private boolean isHome(DTNHost host) {
        return host.toString().startsWith("home");
    }

    private boolean isInternet(DTNHost host) {
        return host.toString().startsWith("inter");
    }

    private boolean isCollector(DTNHost host) {
        return host.toString().startsWith("col");
    }
}
