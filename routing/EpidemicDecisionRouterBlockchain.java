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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EpidemicDecisionRouterBlockchain implements RoutingDecisionEngine {

    /**
     * For Report purpose, maybe needed some variable
     */
    protected LinkedList<Double> resourcesList;
    private static final String THRESHOLD = "threshold";
    private static final int DEFAULT_THRESHOLD = 6;
    private Double lastRecord = Double.MIN_VALUE;
    private int interval;
    private List<Block> minedBlock;
    private int threshold;

    public EpidemicDecisionRouterBlockchain(Settings s) {
        minedBlock = new ArrayList<>();

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

            /*
                Setelah semua localchain sudah diappend ke mainchain, dari
                Collector memberi kabar ke Home bahwa sudah selesai, lalu
                Home memberi tahu para operator proxy bahwa sudah selesai dan
                siap untuk memberi reward ke para miner yang bloknya sudah 
                terpilih dalam proses mining.
         */
            reward_algorithmSix(host, peer);
        }

    }

    /**
     * Mengimplementasikan algoritma penambangan dimana Operator Proxy memilih
     * daftar transaksi terbaik, menugaskannya ke penambang (Miner), dan
     * mencatat waktu penambangan. Blok dengan hasil terbaik kemudian dipilih
     * dan disimpan dalam rantai blok lokal (Localchai).
     *
     * Algoritma bekerja dengan langkah-langkah: 
     * 1. Operator Proxy memilih daftar transaksi terbaik dari kumpulan 
     *    transaksi yang ada.
     * 2. Menugaskan proses penambangan ke Miner yang belum pernah ditugaskan 
     *    sebelumnya.
     * 3. Miner melakukan proses penambangan (proof-of-work) dan mencatat waktu
     *    yang dibutuhkan.
     * 4. Setelah 7 Miner berpartisipasi, blok dengan waktu mining terbaik 
     *    akan dipilih.
     * 5. Blok terpilih ditambahkan ke Localchain dan daftar transaksi diperbarui
     *
     * @param host DTNHost yang bertindak sebagai Operator Proxy untuk mengelola
     * proses penambangan
     * @param peer DTNHost yang bertindak sebagai Miner untuk melakukan operasi
     * penambangan
     */
    private void mining_algorithmOne(DTNHost host, DTNHost peer) {
        if (isOperatorProxy(host)) {
            
            List<List<Transaction>> trx = host.getTrx();
            Localchain localChain = host.getLocalchain();
            String previousHash = localChain.getLatestBlock().getHash();

            // proses mining terus berjalan selama list trx OP masih ada
            if (host.getSelectedBlock() == null && !host.getTrx().isEmpty()) {

                if (isMiner(peer)) {

                    // datangi setiap miner yang baru pertama kali bertemu
                    if (!host.getVisitedMiner().contains(peer)) { 
                        host.getVisitedMiner().add(peer);

                        // pilih transaksi terbaik, tiap miner pasti memilih transaksi yang sama
                        int indexBestTRX = getBestTranx(trx);
                        List<Transaction> bestTransactionList = new ArrayList<>(trx.get(indexBestTRX));
                        
                        // cek tiap transaksi yang ada di list yang miner pilih
                        for (int i = 0; i < bestTransactionList.size(); i++) {
                            
                            // periksa validitas tanda tangan digital tranasaksi
                            if (!bestTransactionList.get(i).verifySignature()) {
                                System.out.println("Transaksi " 
                                        + bestTransactionList.get(i).getTransactionHash() 
                                        + " tidak valid!");
                                bestTransactionList.remove(i);
                            }
                        }
                        
                        // bungkus transaksi ke sebuah blok
                        Block b = new Block(previousHash, 
                                bestTransactionList, 
                                System.currentTimeMillis());
                        b.setFee(getFee(bestTransactionList));
                        b.setMinedBy(peer);

                        // mining blok (menebak nilai nonce)
                        long begin = System.currentTimeMillis();
                        b.mineBlock(localChain.getDifficulty());
                        long end = System.currentTimeMillis();
                        long time = end - begin;

                        b.setIntervalMining(time);

                        minedBlock.add(b);
                    }
                }

                /* Saat sebuah transaksi yang sama sudah dibentuk blok
                *  oleh 7 miner (per area ada 7), maka OperatorProxy bertugas
                *  mencari minedBlock terbaik untuk dipilih ke selectedBlock
                */
                if (host.getVisitedMiner().size() == 7) {

                    // reset catatan kedatangan untuk transaksi-transaksi selanjutnya
                    host.getVisitedMiner().clear();

                    // hapus transaksi terpilih dari list trx OperatorProxy
                    int indexBestTRX = getBestTranx(trx);
                    host.getTrx().remove(indexBestTRX);

                    // pilih blok terbaik, masukkan ke selectedBlock
                    int index = getBestMinedBlock(minedBlock);
                    Block selectedBlock = new Block(minedBlock.get(index));
                    host.setSelectedBlock(selectedBlock);

                    minedBlock.clear();
                }
            }
        }
    }

    /**
    * Mengimplementasikan algoritma verifikasi dimana Operator Proxy memvalidasi
    * blok yang telah ditambang melalui konsensus beberapa Miner. Blok akan 
    * dianggap valid jika mencapai threshold verifikasi tertentu sebelum 
    * ditambahkan ke rantai blok lokal.
    * 
    * Algoritma bekerja dengan langkah-langkah:
    * 1. Operator Proxy memastikan blok yang dipilih valid dengan memverifikasi hash blok
    * 2. Mengirim blok ke Miner yang belum pernah melakukan verifikasi sebelumnya
    * 3. Setiap Miner menghitung ulang hash blok dan membandingkan dengan hash target
    * 4. Jika jumlah verifikasi valid mencapai threshold yang ditentukan:
    *    a. Blok ditambahkan ke rantai blok lokal
    *    b. Reset semua parameter dan status verifikasi
    *    c. Memperbarui status kesiapan penyimpanan jika semua transaksi telah diproses
    *
    * @param host DTNHost yang bertindak sebagai Operator Proxy untuk mengelola proses verifikasi
    * @param peer DTNHost yang bertindak sebagai Miner untuk melakukan operasi verifikasi
    */
    private void verification_algorithmTwo(DTNHost host, DTNHost peer) {
        if (isOperatorProxy(host)) {
            if (host.getSelectedBlock() != null) {

                Localchain localChain = host.getLocalchain();
                Block selectedBlock = host.getSelectedBlock();

                if (isMiner(peer)) {

                    if (!host.getVisitedMiner().contains(peer)) {

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
    * Algoritma penyimpanan dimana Operator Proxy menyimpan rantai blok lokal 
    * yang telah divalidasi ke node Home setelah semua proses penambangan dan 
    * verifikasi selesai.
    * 
    * Algoritma bekerja dengan langkah-langkah:
    * 1. Operator Proxy memeriksa kesiapan status penyimpanan (readyToStore)
    * 2. Menyimpan rantai blok lokal ke node Home yang belum pernah dikunjungi
    * 3. Mencatat riwayat penyimpanan untuk menghindari duplikasi
    * 4. Memberikan output informasi ukuran penyimpanan saat ini
    *
    * @param host DTNHost yang bertindak sebagai Operator Proxy yang menyimpan data
    * @param peer DTNHost yang bertindak sebagai Home sebagai tujuan penyimpanan
    */
    private void storing_algorithmThree(DTNHost host, DTNHost peer) {
        if (isOperatorProxy(host) && isHome(peer)) {
            
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

    /**
    * Algoritma seleksi rantai blok terbaik oleh Collector dari semua rantai 
    * yang tersimpan di node Home untuk diunggah ke jaringan utama.
    * 
    * Algoritma bekerja dengan langkah-langkah:
    * 1. Collector memeriksa kelengkapan rantai blok yang tersimpan di Home
    * 2. Memilih rantai dengan ukuran terpanjang sebagai kandidat terbaik
    * 3. Melakukan perhitungan hash ulang untuk memastikan integritas data
    * 4. Mengunggah rantai terpilih ke jaringan dan memperbarui status sistem
    * 5. Menghentikan proses jika semua transaksi telah diproses
    *
    * @param host DTNHost yang bertindak sebagai Home penyimpan data
    * @param peer DTNHost yang bertindak sebagai Collector penyeleksi rantai
    */
    private void selection_algorithmFour(DTNHost host, DTNHost peer) {
        if (isHome(host)) {

            if (isCollector(peer)) {
                if (peer.isAppendingDone() || host.isAppendingDone()) {
                    return;
                }

                if (peer.getSelectedLocalchain() == null) {

                    if (host.getStoredLocalchains().size() == SimScenario.getInstance().localChainCount) {
                        if (host.getStoredLocalchains().isEmpty()) {
                            System.out.println("SEMUA TRANSAKSI SUDAH DITAMBAHKAN DI BLOCKCHAIN");
                            host.setAppendingDone(true);
                            peer.setAppendingDone(true);
                            return;
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
    * Algoritma penyambungan rantai lokal ke rantai blok utama (main blockchain)
    * melalui node Collector setelah proses seleksi selesai.
    * 
    * Algoritma bekerja dengan langkah-langkah:
    * 1. Collector memverifikasi validitas localchainn terpilih dengan membandingkan hash
    * 2. Jika valid, localchain disambungkan ke blockchain utama di node Internet
    * 3. Memperbarui counter chain yang tersisa dan menampilkan status akhir
    * 4. Memberikan output informasi real-time tentang progres penyambungan
    *
    * @param host DTNHost yang bertindak sebagai Collector pelaksana penyambungan
    * @param peer DTNHost yang bertindak sebagai Internet penyimpan rantai utama
    */
    private void appending_algorithmFive(DTNHost host, DTNHost peer) {
        if (isCollector(host) && isInternet(peer)) {
            if (host.isAppendingDone()) {
                return;
            }

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

    /**
    * Algoritma distribusi reward kepada miner berdasarkan kontribusi penambangan blok.
    * 
    * Algoritma bekerja dalam 2 fase:
    * A. Informasikan status selesai:
    *    1. Home memberitahu Operator Proxy bahwa proses penyambungan selesai
    *    2. Operator Proxy menandai proses sebagai selesai
    * 
    * B. Pembagian reward:
    *    1. Operator Proxy memverifikasi miner yang belum menerima reward
    *    2. Menghitung total fee dari semua blok yang ditambang miner tersebut
    *    3. Menambahkan balance ke wallet miner
    *    4. Mencatat miner yang sudah menerima reward
    *
    * @param host Operator Proxy yang bertindak sebagai penerima/prosesor reward
    * @param peer miner yang bertindak sebagai sumber/pemberi reward
    */
    private void reward_algorithmSix(DTNHost host, DTNHost peer) {        
        if (isOperatorProxy(host) && isHome(peer)) {
            
            /* Informasikan dulu dari home ke para operator proxy */
            if (peer.isAppendingDone()) {
                host.setAppendingDone(true);
            }
            
            /* Jika OperatorProxy ternyata sudah selesai membagikan reward,
               catat ke Home 
            */
            if (host.isDoneReward()) {
                peer.getConfirmedDoneOperatorProxy().add(host);
            }
            
            /* Mekanisme trasaksi selesai jika semua operator proxy 
               sudah selesai membagikan reward (8 area)
            */
            if (peer.getConfirmedDoneOperatorProxy().size() == 8) {
                System.out.println("MEKANISME TRANSAKSI SELESAI");
                System.exit(0);
            }
        }

        /* Mulai bagikan fee ke miner */
        if (isMiner(host) && isOperatorProxy(peer)) {
            if (peer.isDoneReward()) {return;}
            
            if (peer.isAppendingDone() && !peer.getRewardedMiners().contains(host)) {
                List<Block> list = peer.getLocalchain().getChain();

                Iterator<Block> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Block b = iterator.next();
                    DTNHost miner = b.getMinedBy();
                    double fee = b.getFee();

                    if (miner.equals(host)) {
                        System.out.println("Memberikan reward ke " + miner + ".....");
                        host.getWallet().addBalance(fee);
                        iterator.remove();
                    }
                }

                peer.getRewardedMiners().add(host);
                
                if (list.isEmpty()) {
                    System.out.println(peer +" telah selesai membagikan reward");
                    peer.setDoneReward(true);
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

        return 0.01 * total;
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
        return isMiner(thisHost) && (isOperatorProxy(otherHost) || isMiner(otherHost));
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return m.getTo() == otherHost;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
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
