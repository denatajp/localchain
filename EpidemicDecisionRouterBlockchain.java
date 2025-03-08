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


/**
 *
 * @author Afra Rian
 */
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

        if (isOperatorProxy(host) && isMiner(peer)) {
            
            // Tangkap list transaksi dari host (operator proxy)
            List<List<Transaction>> trx = host.getTrx();
            List<Transaction> list = getBestTranx(trx);
            
            // Ambil Localchain dari host (operator proxy)
            Localchain localChain = host.getLocalchain();
            
            String previousHash = localChain.getLatestBlock().getHash();
            
            Block b = new Block(previousHash, list, System.currentTimeMillis());
            
            // Catat waktu mining tiap block
            long begin = System.currentTimeMillis();
            b.mineBlock(localChain.getDifficulty());
            long end = System.currentTimeMillis();
            long time = end - begin;
            b.setIntervalMining(time);
            System.out.println("Waktu mining : " + time);
            
            minedBlock.add(b);
        }
        
        if (isOperatorProxy(host) && isHome(peer)) {
            int index = -1;
            long min = 0;
            for (Block b : minedBlock) {
                if (min == 0) {
                    min = b.getIntervalMining();
                    index++;
                } 
                
                if (b.getIntervalMining() < min) {
                    min = b.getIntervalMining();
                    index++;
                }
            }
            Block selectedBlock = new Block(minedBlock.get(index));
            host.setSelectedBlock(selectedBlock);
            minedBlock.clear();
            
            System.out.println("");
            System.out.println("Selected Block : " + host.getSelectedBlock() + " dengan waktu " + host.getSelectedBlock().getIntervalMining());
        }
    }

    
    /**
     * Find the best transaction with biggest fee
     * @param trx
     * @return 
     */
    private List<Transaction> getBestTranx(List<List<Transaction>> trx) {
        int index = -1;
        double maxTotal = 0;
        for (List<Transaction> l : trx) {
            double tempTotal = 0;
            for (Transaction t : l) {
                tempTotal += t.getAmount();
            }
            if (tempTotal > maxTotal) {
                maxTotal = tempTotal;
                index++;
            }
        }

        return trx.get(index);
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

