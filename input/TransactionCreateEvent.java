/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import Blockchain.Transaction;
import core.DTNHost;
import core.Message;
import core.World;

/**
 * External event untuk pembuatan pesan custom dengan Transaction. Hanya
 * node miner yang dapat membangkitkan pesan.
 */
public class TransactionCreateEvent extends MessageEvent {

    private int size;
    private int responseSize;
    private Transaction tr;

    /**
     * Creates a message creation event with a optional response request
     *
     * @param from The creator of the message
     * @param to Where the message is destined to
     * @param id ID of the message
     * @param size Size of the message
     * @param responseSize Size of the requested response message or 0 if no
     * response is requested
     * @param time Time, when the message is created
     * @param tr Transaction, miner create a transaction
     */
    public TransactionCreateEvent(int from, int to, String id, int size,
            int responseSize, double time, Transaction tr) {
        super(from, to, id, time);
        this.size = size;
        this.responseSize = responseSize;
        this.tr = tr;
    }

    /**
     * Bangkitkan pesan khusus node miner saja
     */
    @Override
    public void processEvent(World world) {
        DTNHost to = world.getNodeByAddress(this.toAddr);
        DTNHost from = world.getNodeByAddress(this.fromAddr);
        
        Message m = new Message(from, to, this.id, this.size);
        m.setResponseSize(this.responseSize);
        
        // pastikan objek transaction tidak null
        if (this.tr != null) {
            m.addProperty("transaction", this.tr);
        }
        
        /**
         * Bangkitkan pesan pada node miner, kenapa range ID 1-56? karena
         * ada 8 area dan masing-masing area ada 7 miner, jadi 8x7 = 56, jadi
         * ID miner antara 1 sampai 56 saja.
         */
        if (this.fromAddr != 0 && this.fromAddr <57 ) {
            from.createNewMessage(m);
        }
        
    }

    public Transaction getTransaction() {
        return this.tr;
    }
    
    @Override
    public String toString() {
        return super.toString() + " [" + fromAddr + "->" + toAddr + "] "
                + "size:" + size + " CREATE";
    }
}
