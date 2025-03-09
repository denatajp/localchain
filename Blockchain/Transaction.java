package Blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Transaction {

    private String sender;
    private String receiver;
    private double amount;
    private long timestamp;
    private String transactionHash;

    public Transaction(String sender, String receiver, double amount, long timestamp, double fee) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        this.transactionHash = calculateHash();
    }

    private String calculateHash() {
        String data = sender + receiver + amount + timestamp;
        return applySHA256(data);
    }

    private String applySHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

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
