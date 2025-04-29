package Blockchain;

import java.util.ArrayList;
import java.util.List;

/**
 * Blockchain digunakan sebagai rantai utama yang tersimpan di Internet
 * @author Denata
 */
public class Blockchain {

    private List<Block> chain;
    private final int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
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
     * Menambahkahkan blok baru ke blockchain
     * @param newBlock Blok yang akan ditambah
     */
    public void addBlock(Block newBlock) {
        chain.add(newBlock);
    }

    public int chainSize() {
        return chain.size();
    }
    
    /**
     * Menambahkan block dari local chain ke blockchain utama
     * @param localChain - Chain lokal yang mau diambil block-nya
     */
    public void addBlockFromLocalChain(Localchain localChain) {
        List<Block> blockFromLocalchain = new ArrayList<>(localChain.getChain());

        for (Block b : blockFromLocalchain) {
            String previousHash = getLatestBlock().getHash();
            b.setPreviousHash(previousHash);
            // b.recalculateHash(difficulty);
            if (!b.recalculateHash(difficulty)) {
                System.out.println("Block " + b.getHash() + " bukan dari Localchain!");
                continue;
            }
            addBlock(b);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==== BLOCKCHAIN ====\n");

        for (Block block : chain) {
            sb.append("Prev Hash: ").append(block.getPreviousHash()).append("\n")
                    .append("Hash    : ").append(block.getHash()).append("\n")
                    .append("Mined By: ").append(block.getMinedBy() != null ? block.getMinedBy().toString() : "Unknown").append("\n")
                    .append("-----------------------\n");
        }

        return sb.toString();
    }

}
