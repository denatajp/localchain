package routing;

import Blockchain.Block;
import Blockchain.Localchain;
import Blockchain.Transaction;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
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
    private Double lastRecord = Double.MIN_VALUE;
    private int interval;
    private List<Block> minedBlock;

    public EpidemicDecisionRouterBlockchain(Settings s) {
        minedBlock = new ArrayList<>();
        if (s.contains(TOTAL_CONTACT_INTERVAL)) {
            interval = s.getInt(TOTAL_CONTACT_INTERVAL);
        } else {
            interval = DEFAULT_CONTACT_INTERVAL;
        }
    }

    public EpidemicDecisionRouterBlockchain(EpidemicDecisionRouterBlockchain proto) {
        minedBlock = new ArrayList<>();
        resourcesList = new LinkedList<>();
        interval = proto.interval;
        lastRecord = proto.lastRecord;
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

        //jalankan algoritma pertama MINING
        if (isOperatorProxy(host) && !host.getTrx().isEmpty()) {
            mining_algorithmOne(host, peer);
        }

    }

    /**
     * Implements the mining algorithm where an Operator Proxy selects the best
     * transaction list, assigns it to a miner, and records the mining time. The
     * best-mined block is then selected and stored in the local blockchain.
     *
     * @param host The DTNHost that acts as an Operator Proxy and manages the
     * mining process.
     * @param peer The DTNHost that acts as a Miner and performs the mining
     * operation.
     */
    private void mining_algorithmOne(DTNHost host, DTNHost peer) {

        if (isOperatorProxy(host)) {

            List<List<Transaction>> trx = host.getTrx();
            Localchain localChain = host.getLocalchain();
            String previousHash = localChain.getLatestBlock().getHash();

            if (isMiner(peer)) {

                if (!host.getVisitedMiner().containsKey(peer)) {

                    host.getVisitedMiner().put(peer, System.currentTimeMillis());
                    System.out.println("Visited Miner : " + host.getVisitedMiner().size());

                    int indexBestTRX = getBestTranx(trx);
                    List<Transaction> bestTransactionList = new ArrayList<>(trx.get(indexBestTRX));

                    Block b = new Block(previousHash, bestTransactionList, System.currentTimeMillis());
                    b.setFee(getFee(bestTransactionList));
                    b.setMinedBy(peer);

                    long begin = System.currentTimeMillis();

                    System.out.println("Miner " + peer + " is mining block....");
                    b.mineBlock(localChain.getDifficulty());

                    long end = System.currentTimeMillis();
                    long time = end - begin;

                    b.setIntervalMining(time);
                    System.out.println("Mining time : " + time + " ms");
                    System.out.println("Mined by: " + peer);
                    System.out.println("");

                    minedBlock.add(b);
                }
            }

////            if (isHome(peer)) {
            if (host.getVisitedMiner().size() == 15) {

                host.getVisitedMiner().clear();

                // hapus transaksi terpilih dari trx
                int indexBestTRX = getBestTranx(trx);
                host.getTrx().remove(indexBestTRX);

                // ambil blok terbaik dengan mining tercepat
                int index = getBestMinedBlock(minedBlock);

                Block selectedBlock = new Block(minedBlock.get(index));
                host.setSelectedBlock(selectedBlock);

                // reset bestTransactionList minedBlock
                minedBlock.clear();

                System.out.println(selectedBlock);

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
        return false;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return !thisHost.getRouter().hasMessage(m.getId());
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        return true;
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

    private boolean isCollector(DTNHost host) {
        return host.toString().startsWith("col");
    }
}
