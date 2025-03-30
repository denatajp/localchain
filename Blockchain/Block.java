package Blockchain;

import core.DTNHost;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Blok yang merupakan class penting yang digunakan dalam mekanisme
 * transaksi di Blockchain dimana membungkus transaksi-transaksi yang ada
 * @author Denata
 */
public class Block {

    /**
     * Hash dari blok sebelumnya dalam blockchain. 
     * Ini kayak 'tanda pengenal' blok sebelumnya biar 
     * rantai bloknya nyambung.
     */
    private String previousHash;
    
    /**
    * Daftar transaksi yang ada di dalam blok ini. 
    * Kumpulan transaksi ini yang nanti bakal diverifikasi 
    * dan dicatat di blockchain.
    */
    private List<Transaction> transactions;
    
    /**
     * Waktu pembuatan blok (dalam format timestamp). 
     * Ngecatat kapan blok ini dibuat biar gak bisa diubah-ubah timeline-nya.
     */    
    private long timestamp;
    
    /**
    * Angka acak yang dipake waktu mining blok. 
    * Ini kaya "tebakan" biar hash bloknya sesuai sama kriteria kesulitan 
    * (misal: Difficulty nya 5, maka harus ada 5 angka 0 di depan blockHash).
    */
    private int nonce;
    
    /**
     * Hash unik untuk blok ini. 
     * Dihitung dari gabungan previousHash, transactions, timestamp, 
     * dan nonce. Ini kayak sidik jari blok.
     */
    private String blockHash;
    
    /**
     * Waktu (dalam milidetik) yang dibutuhkan untuk menambang blok ini. 
     * Menggambarkan seberapa susah blok ini ditambang.
     */
    private long intervalMining;
    
    /**
     * Penambang (node) yang berhasil menambang blok ini. 
     * Biasanya dapat reward karena udah ngehitung hash yang valid.
     */
    private DTNHost minedBy;
    
    /**
     * Total biaya transaksi yang dikumpulin di blok ini. 
     * Biaya ini jadi insentif buat penambang.
     */
    private double fee;

    /**
     * Default constructor, digunakan khusus generic blok
     */
    public Block() {
        this.previousHash = "0"; //set previous hash 0 dulu
    }

    public Block(String previousHash, List<Transaction> transactions, long timestamp) {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = timestamp;
        this.nonce = 0;
        this.intervalMining = 0;
        this.blockHash = calculateHash();
    }

    /**
     * Copy constructor yang dipakai nanti saat mau proses pemindahan, untuk
     * mengantisipasi kasus "Passing by References" pada objek.
     * @param other Blok yang mau dicopy
     */
    public Block(Block other) {
        this.previousHash = other.previousHash;
        this.transactions = new ArrayList<>(other.transactions); // Copy list transaksi
        this.timestamp = other.timestamp;
        this.nonce = other.nonce;
        this.blockHash = other.blockHash;
        this.intervalMining = other.intervalMining;
        this.fee = other.fee;
        this.minedBy = other.minedBy;
    }
    
    /**
    * Hitung hash block dari previous hash, transaksi, timestamp, dan nonce
    * @return hash dari blok
    */
    public String calculateHash() {
        StringBuilder data = new StringBuilder(previousHash + timestamp + nonce);
        for (Transaction tx : transactions) {
            data.append(tx.getTransactionHash());
        }
        return SecureTransaction.applySha256(data.toString());
    }

    /**
     * Method calculateHash saat ingin diappend ke Blockchain. Karena nanti ada
     * perubahan previousHash, cek dahulu noncenya saat dicalculate memenuhi
     * difficulty atau tidak. 
     * Jika ya simpan dan pakai nonce sekarang.
     * Jika tidak, cari nonce lagi sampai hash memenuhi target
     * @param difficulty target kesulitan pada localchain/blockchain
     */
    public void recalculateHash(int difficulty) {
        String newHash = calculateHash();

        // Cek apakah hash yang dihasilkan memenuhi kriteria difficulty
        if (isHashValid(newHash, difficulty)) {
            this.blockHash = newHash;
            
        } else {
            // Jika tidak valid, lakukan mining ulang
            this.nonce = 0; // Reset nonce
            mineBlock(difficulty); // Lakukan mining ulang
        }
    }

    /**
    * Ngecek apakah hash block valid sesuai difficulty
    * @param hash Hash yang mau dicek
    * @param difficulty Target jumlah angka 0
    * @return true kalo hash memenuhi syarat
    */
    private boolean isHashValid(String hash, int difficulty) {
        String target = repeatZero(difficulty); // Buat target dengan jumlah 0 di awal
        return hash.substring(0, difficulty).equals(target);
    }

    /**
    * Menambang block (mencari nilai nonce) sampai hashnya sesuai 
    * difficulty (banyak angka 0 di depan hash)
    * @param difficulty Target jumlah angka 0 di awal hash
    */
    public void mineBlock(int difficulty) {
        String target = repeatZero(difficulty);
        while (!blockHash.substring(0, difficulty).equals(target)) {
            nonce++;
            blockHash = calculateHash();
        }
    }

    /**
     * Method bantuan untuk membuat karakter "0" sebanyak n kali. (n = difficulty)
     * @param count target difficulty
     * @return karakter "0"
     */
    private String repeatZero(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("0");
        }
        return sb.toString();
    }

    public void setBlockHash(String blockHash) {this.blockHash = blockHash;}

    public long getIntervalMining() {return intervalMining;}

    public void setIntervalMining(long intervalMining) {this.intervalMining = intervalMining;}
    
    public String getHash() {return blockHash;}

    public void setPreviousHash(String previousHash) {this.previousHash = previousHash;}

    public String getPreviousHash() {return previousHash;}

    public DTNHost getMinedBy() {return minedBy;}

    public void setMinedBy(DTNHost minedBy) {this.minedBy = minedBy;}

    public double getFee() {return fee;}

    public void setFee(double fee) {this.fee = fee;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================== "
                + "BLOCK "
                + "======================================\n")
                .append("Previous Hash   : ").append(previousHash).append("\n")
                .append("Timestamp       : ").append(timestamp).append("\n")
                .append("Nonce           : ").append(nonce).append("\n")
                .append("Block Hash      : ").append(blockHash).append("\n")
                .append("Interval Mining : ").append(intervalMining).append("\n")
                .append("Mined By        : ").append(minedBy).append("\n")
                .append("Fee             : ").append(fee).append("\n")
                .append("Transactions    : \n");

        for (Transaction t : transactions) {
            sb.append(t.toString()).append("\n"); // Memanggil toString() Transaction
        }

        sb.append("======================================"
                + "======================================\n");
        return sb.toString();
    }
}
