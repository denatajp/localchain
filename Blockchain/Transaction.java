package Blockchain;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Class Transaction dimana objek transaksi dibuat oleh para miner
 * @author Denata
 */
public class Transaction {

    private PublicKey sender;
    private PublicKey receiver;
    private double amount;
    private long timestamp;
    private String transactionHash;
    private byte[] signature;

    public Transaction(PublicKey sender, PublicKey receiver, double amount, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        this.transactionHash = calculateHash();
    }

    /**
    * Hitung hash transaksi dari sender, receiver, amount, dan timestamp
    */
    private String calculateHash() {
        return SecureTransaction.applySha256
                (SecureTransaction.getStringFromKey(sender) 
                + SecureTransaction.getStringFromKey(sender)
                + amount + timestamp);
    }

    /**
    * Bikin tanda tangan digital untuk transaksi ini pake private key
    * @param privateKey - Kunci pribadi si pengirim
    */
    public void generateSignature(PrivateKey privateKey) {
        String data = SecureTransaction.getStringFromKey(sender) 
                + SecureTransaction.getStringFromKey(receiver) 
                + amount;
        signature = SecureTransaction.applyECDSASig(privateKey, data);
    }
    
    /**
    * Ngecek apakah tanda tangan transaksi ini valid
    * @return true kalo tanda tangan cocok
    */
    public boolean verifySignature() {
        String data = SecureTransaction.getStringFromKey(sender) 
                + SecureTransaction.getStringFromKey(receiver) 
                + amount;
        return SecureTransaction.verifyECDSASig(sender, data, signature);
    }
    
    public String getTransactionHash() {return transactionHash;}

    public double getAmount() {return amount;}

    public void setAmount(double amount) {this.amount = amount;}

    @Override
    public String toString() {
        return String.format(
                "  - TRANSACTION:\n"
                + "    Sender    : %s\n"
                + "    Receiver  : %s\n"
                + "    Amount    : %f\n"
                + "    Timestamp : %d\n"
                + "    Hash      : %s",
                sender, receiver, amount, timestamp, transactionHash
        );
    }
}
