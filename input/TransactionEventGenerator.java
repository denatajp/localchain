/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import Blockchain.Transaction;
import core.DTNHost;
import java.util.Random;

import core.Settings;
import core.SettingsError;
import core.SimScenario;
import java.security.PublicKey;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generate Event pembuatan pesan yang berisi transaksi.
 */
public class TransactionEventGenerator implements EventQueue {

    private int eventCount = 0;
    /**
     * Message size range -setting id ({@value}). Can be either a single value
     * or a range (min, max) of uniformly distributed random values. Defines the
     * message size (bytes).
     */
    public static final String MESSAGE_SIZE_S = "size";
    /**
     * Message creation interval range -setting id ({@value}). Can be either a
     * single value or a range (min, max) of uniformly distributed random
     * values. Defines the inter-message creation interval (seconds).
     */
    public static final String MESSAGE_INTERVAL_S = "interval";
    /**
     * Sender/receiver address range -setting id ({@value}). The lower bound is
     * inclusive and upper bound exclusive.
     */
    public static final String HOST_RANGE_S = "hosts";
    /**
     * (Optional) receiver address range -setting id ({@value}). If a value for
     * this setting is defined, the destination hosts are selected from this
     * range and the source hosts from the {@link #HOST_RANGE_S} setting's
     * range. The lower bound is inclusive and upper bound exclusive.
     */
    public static final String TO_HOST_RANGE_S = "tohosts";

    /**
     * Message ID prefix -setting id ({@value}). The value must be unique for
     * all message sources, so if you have more than one message generator, use
     * different prefix for all of them. The random number generator's seed is
     * derived from the prefix, so by changing the prefix, you'll get also a new
     * message sequence.
     */
    public static final String MESSAGE_ID_PREFIX_S = "prefix";
    /**
     * Message creation time range -setting id ({@value}). Defines the time
     * range when messages are created. No messages are created before the first
     * and after the second value. By default, messages are created for the
     * whole simulation time.
     */
    public static final String MESSAGE_TIME_S = "time";

    /**
     * Time of the next event (simulated seconds)
     */
    protected double nextEventsTime = 0;
    /**
     * Range of host addresses that can be senders or receivers
     */
    protected int[] hostRange = {0, 0};
    /**
     * Range of host addresses that can be receivers
     */
    protected int[] toHostRange = null;
    /**
     * Next identifier for a message
     */
    private int id = 0;
    /**
     * Prefix for the messages
     */
    protected String idPrefix;
    /**
     * Size range of the messages (min, max)
     */
    private int[] sizeRange;
    /**
     * Interval between messages (min, max)
     */
    private int[] msgInterval;
    /**
     * Time range for message creation (min, max)
     */
    protected double[] msgTime;

    /**
     * Random number generator for this Class
     */
    protected Random rng;

    /**
     * Constructor, initializes the interval between events, and the size of
     * messages generated, as well as number of hosts in the network.
     *
     * @param s Settings for this generator.
     */
    public TransactionEventGenerator(Settings s) {
        this.sizeRange = s.getCsvInts(MESSAGE_SIZE_S);
        this.msgInterval = s.getCsvInts(MESSAGE_INTERVAL_S);
        this.hostRange = s.getCsvInts(HOST_RANGE_S, 2);
        this.idPrefix = s.getSetting(MESSAGE_ID_PREFIX_S);

        if (s.contains(MESSAGE_TIME_S)) {
            this.msgTime = s.getCsvDoubles(MESSAGE_TIME_S, 2);
        } else {
            this.msgTime = null;
        }
        if (s.contains(TO_HOST_RANGE_S)) {
            this.toHostRange = s.getCsvInts(TO_HOST_RANGE_S, 2);
        } else {
            this.toHostRange = null;
        }

        /* if prefix is unique, so will be the rng's sequence */
        this.rng = new Random(idPrefix.hashCode());

        if (this.sizeRange.length == 1) {
            /* convert single value to range with 0 length */
            this.sizeRange = new int[]{this.sizeRange[0], this.sizeRange[0]};
        } else {
            s.assertValidRange(this.sizeRange, MESSAGE_SIZE_S);
        }
        if (this.msgInterval.length == 1) {
            this.msgInterval = new int[]{this.msgInterval[0],
                this.msgInterval[0]};
        } else {
            s.assertValidRange(this.msgInterval, MESSAGE_INTERVAL_S);
        }
        s.assertValidRange(this.hostRange, HOST_RANGE_S);

        if (this.hostRange[1] - this.hostRange[0] < 2) {
            if (this.toHostRange == null) {
                throw new SettingsError("Host range must contain at least two "
                        + "nodes unless toHostRange is defined");
            } else if (toHostRange[0] == this.hostRange[0]
                    && toHostRange[1] == this.hostRange[1]) {
                // XXX: teemuk: Since (X,X) == (X,X+1) in drawHostAddress()
                // there's still a boundary condition that can cause an
                // infinite loop.
                throw new SettingsError("If to and from host ranges contain"
                        + " only one host, they can't be the equal");
            }
        }

        /* calculate the first event's time */
        this.nextEventsTime = (this.msgTime != null ? this.msgTime[0] : 0)
                + msgInterval[0]
                + (msgInterval[0] == msgInterval[1] ? 0
                        : rng.nextInt(msgInterval[1] - msgInterval[0]));
    }

    /**
     * Draws a random host address from the configured address range
     *
     * @param hostRange The range of hosts
     * @return A random host address
     */
    protected int drawHostAddress(int hostRange[]) {
        if (hostRange[1] == hostRange[0]) {
            return hostRange[0];
        }
        return hostRange[0] + rng.nextInt(hostRange[1] - hostRange[0]);
    }

    /**
     * Generates a (random) message size
     *
     * @return message size
     */
    protected int drawMessageSize() {
        int sizeDiff = sizeRange[0] == sizeRange[1] ? 0
                : rng.nextInt(sizeRange[1] - sizeRange[0]);
        return sizeRange[0] + sizeDiff;
    }

    /**
     * Generates a (random) time difference between two events
     *
     * @return the time difference
     */
    protected int drawNextEventTimeDiff() {
        int timeDiff = msgInterval[0] == msgInterval[1] ? 0
                : rng.nextInt(msgInterval[1] - msgInterval[0]);
        return msgInterval[0] + timeDiff;
    }

    /**
     * Buat tujuan pesan berdasarkan area masing-masing. Tujuan selalu
     * ditujukan ke Operator Proxy di setiap areanya.
     *
     * @param from ID pengirim untuk dilihat area berapa
     * @return ID penerima (Operator Proxy) berdasarkan areanya
     */
    protected int drawToAddress(int from) {
        int minersInGroup = SimScenario.getInstance().getMinersInGroup();
        int to;

        if (from >= 1 && from <= 1 * minersInGroup) {   // area 1
            to = 8 * minersInGroup + 1;
        } else if (from <= 2 * minersInGroup) {        // area 2
            to = 8 * minersInGroup + 2;
        } else if (from <= 3 * minersInGroup) {        // area 3
            to = 8 * minersInGroup + 3;
        } else if (from <= 4 * minersInGroup) {        // area 4      
            to = 8 * minersInGroup + 4;
        } else if (from <= 5 * minersInGroup) {        // area 5
            to = 8 * minersInGroup + 5;
        } else if (from <= 6 * minersInGroup) {        // area 6
            to = 8 * minersInGroup + 6;
        } else if (from <= 7 * minersInGroup) {        // area 7
            to = 8 * minersInGroup + 7;
        } else {                        // area 8
            to = 8 * minersInGroup + 8;
        }

        return to;
    }
    
    /**
     * Buat tujuan transaksi. Berbeda dengan pesan, karena transaksi bisa
     * di luar area, jadi pada simulasi ini random miner di area mana saja.
     * @param from ID pengirim
     * @return ID penerima (miner 1-56) tapi bukan dirinya sendiri
     */
    protected int drawToAddressTrx(int from) {
            int to;
            do {
                to = 1+rng.nextInt(56);
            } while (to==from);
            
        return to;
    }

    /**
     * Bangkitkan event pembuatan pesan
     *
     * @see input.EventQueue#nextEvent()
     */
    @Override
    public ExternalEvent nextEvent() {
        
        // Inisialisasi property pesan
        int responseSize = 0;
        int msgSize =drawMessageSize() ;
        int interval= drawNextEventTimeDiff();
        int from= drawHostAddress(this.hostRange);
        int to= drawToAddress(from);
        
        //Akses Public dan Private key penerima dan pengirim untuk proses kriptografi
        DTNHost senderHost = SimScenario.getInstance().getHosts().get(from);
        DTNHost receiverHost = SimScenario.getInstance().getHosts().get(drawToAddressTrx(from));
        PublicKey senderPublicKey = senderHost.getWallet().getPublicKey();
        PublicKey receiverPublicKey = receiverHost.getWallet().getPublicKey();
        
        // Pastikan hanya miner saja yang dapat membangkitkan transaksi
        if (!senderHost.toString().startsWith("min") || senderHost.getWallet() == null) {
            return null;
        }
        
        // Inisialisasi property transaksi
        double amount = ThreadLocalRandom.current().nextDouble(10, 1000);
        long timestamp = System.currentTimeMillis();
        
        // Bangkitkan transaksi
        Transaction tr = new Transaction(senderPublicKey, receiverPublicKey, amount, timestamp);
        tr.generateSignature(senderHost.getWallet().getPrivateKey());
        TransactionCreateEvent tce = new TransactionCreateEvent(from, to, "TRX" + eventCount++,
                msgSize, responseSize, this.nextEventsTime, tr);
        
        this.nextEventsTime += interval;

        if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
            /* next event would be later than the end time */
            this.nextEventsTime = Double.MAX_VALUE;
        }

        return tce;
    }

    /**
     * Returns next message creation event's time
     *
     * @see input.EventQueue#nextEventsTime()
     */
    @Override
    public double nextEventsTime() {
        return this.nextEventsTime;
    }

    /**
     * Returns a next free message ID
     *
     * @return next globally unique message ID
     */
    protected String getID() {
        this.id++;
        return idPrefix + this.id;
    }
}
