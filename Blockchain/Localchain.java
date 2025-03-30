package Blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Blockchain lokal yang dimiliki oleh setiap Operator Proxy
 * @author Denata
 */
public class Localchain {

    private List<Block> chain;
    private int difficulty;
    private String name;
    private String hash;

    /**
    * Constructor, Buat chain baru dengan tingkat kesulitan mining
    * @param difficulty Seberapa banyak angka 0 di awal hash yang diperlukan
    */
    public Localchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
    }

    /**
    * Salin isi chain dari chain lain (biar gak keubah aslinya)
    * @param other Localchain yang mau di-copy
    */
    public Localchain(Localchain other) {
        this.chain = other.chain;
        this.difficulty = other.difficulty;
        this.name = other.name;
        this.hash = other.hash;
    }

    /**
    * Ngambil hash total chain
    * @return 
    */
    public String getHash() {return hash;}


    /**
    * Hitung hash total dari semua block di chain, pake SHA-256
    * @return hash dari penggabungan semua hash blok
    */
    public String calculateHash() {
        String totalHash = "";
        
        for (int i = 0; i < chain.size(); i++) {
            String h = chain.get(i).getHash();
            totalHash = totalHash + " + " + h;
        }
        
        StringBuilder data = new StringBuilder(totalHash);

        return applySHA256(data.toString());
    }

    /**
    * Bikin hash SHA-256 dari input (buat hash blok)
    * @param input - Data yang mau di-hash
    * @return Hash dalam bentuk hex string
    */
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

    /**
    * Ambil block terakhir di chain. Kalo kosong, return block kosong
    * @return objek blok paling terakhir
    */
    public Block getLatestBlock() {
        if (chain.isEmpty()) {
                return new Block();
        }
        
        return chain.get(chain.size() - 1);
    }

    /**
    * Tambah block baru ke chain. Otomatis set previous hash
    * @param newBlock - Block yang mau ditambah
    */
    public void addBlock(Block newBlock) {
        if (chain.isEmpty()) {
            newBlock.setPreviousHash("0");
            chain.add(newBlock);
        }
        else{
            newBlock.setPreviousHash(getLatestBlock().getHash());
            chain.add(newBlock);
        }
        
    }

    /**
    * Hitung jumlah block di chain
     * @return jumlah blok
    */
    public int chainSize() {
        return chain.size();
    }

    public void setHash(String hash) {this.hash = hash;}
    
    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public int getDifficulty() {return difficulty;}

    public List<Block> getChain() {return chain;}

    public void setChain(List<Block> chain) {this.chain = chain;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================ LOCALCHAIN ================================\n");
        sb.append("Localchain Title  : ").append(name).append("\n");
        sb.append("Difficulty Level  : ").append(difficulty).append("\n");
        sb.append("Localchain Hash   : ").append(hash).append("\n");

        for (Block block : chain) {
            sb.append(block.toString()).append("\n");
        }
        sb.append("============================================================================\n");
        return sb.toString();
    }

}
