package Blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<Block> chain;
    private final int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        // Genesis block (blok pertama) harus ditambahkan saat blockchain dibuat
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block("0", null, 0);
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        chain.add(newBlock);
    }
    public void addBlockFromLocalChain(Localchain localChain){
        List<Block> blockFromLocalchain = new ArrayList<>(localChain.getChain());
        for (Block block : blockFromLocalchain) {
            addBlock(block);
            
        }
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Periksa apakah hash saat ini masih valid
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            // Periksa apakah hash sebelumnya cocok dengan hash blok sebelumnya
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }
        return true;
    }

    public void printBlockchain() {
        for (Block block : chain) {
//            System.out.println("Index       : " + block.getIndex());
//            System.out.println("Timestamp   : " + block.getTimestamp());
//            System.out.println("Data        : " + block.getData());
            System.out.println("Hash        : " + block.getHash());
            System.out.println("Prev Hash   : " + block.getPreviousHash());
            System.out.println("-------------------------------");
        }
    }
}

