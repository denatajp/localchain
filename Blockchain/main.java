package Blockchain;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;


/**
 * main class untuk percobaan bermacam-macam code
 * @author Denata
 */
public class main {

    private static int getBestTranx(List<List<Transaction>> trx) {
        int index = -1;
        double maxTotal = 0;
//        for (List<Transaction> l : trx) {
//            double tempTotal = 0;
//            for (Transaction t : l) {
//                tempTotal += t.getAmount();
//            }
//            if (tempTotal > maxTotal) {
//                maxTotal = tempTotal;
//                ++index;
//            }
//        }

        for (int i = 0; i < trx.size(); i++) {
            double tempTotal = 0;
            for (Transaction t : trx.get(i)) {
                tempTotal += t.getAmount();
            }
            
            System.out.println("total amount list ke-" + i + ": " + tempTotal);
            
            if (tempTotal > maxTotal) {
                maxTotal = tempTotal;
                index = i; // Simpan indeks yang benar
            }
        }

        return index;
    }

        private static String applySHA256(String input) {
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
    
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        String hash1 ="00055c6aa36d600349f483d1234d3d413bac0424857c76917a2d0d8eaedfa458";
        String hash2 ="b7e79e72ea9afcdf8d2dc0b42b1946aa72c41afc499de2d7dd450061788b2a4f";
        String hash3 ="000ef7fe92a8f8ed9800a27c5fef89e40e9a3c7ce16f56b5c53d8f2d8567c4e1";
        String total = hash1+hash2+hash3;
        System.out.println("Hash Localchain : "+ applySHA256(total));
        BigInteger decimalHash1 = new BigInteger(hash1, 16);
        BigInteger decimalHash2 = new BigInteger(hash2, 16);
        System.out.println(decimalHash1);
        System.out.println(decimalHash2);
        System.out.println(decimalHash1.compareTo(decimalHash2));
        
//        List<List<Transaction>> trx = Inisialisasi.inisialisasi(8);
//        System.out.println("Transaksi pertama : \n" + trx.get(0));
//        System.out.println("");
//        System.out.println("Transaksi kedua : \n" + trx.get(1));
//        System.out.println("");
//        System.out.println("Transaksi ketiga : \n" + trx.get(2));
//        System.out.println("");
//        System.out.println("Transaksi keempat : \n" + trx.get(3));
//        System.out.println("");
//        System.out.println("Transaksi kelima : \n" + trx.get(4));
//
//        int indexTerbaik = getBestTranx(trx);
//        System.out.println("Index terbaik : " + indexTerbaik);
//        double total = 0;
//        double fee = 0;
//        double amount=0;
//        double totalAmount = 0;
//        for (List<Transaction> list : trx) {
//            for (int i = 0; i < list.size(); i++) {
//                fee = list.get(i).getFee();
//                amount = list.get(i).getAmount();
//            }
//            total = total + fee;
//            totalAmount = totalAmount + amount;
//            
//        }
//        
////        System.out.println(total);
////        System.out.println(totalAmount);
//        int index = -1;
//        double maxTotal = 0;
//        for (List<Transaction> l : trx) {
//            double tempTotal = 0;
//            for (Transaction t : l) {
//                tempTotal += t.getAmount();
//            }
//            if (tempTotal > maxTotal) {
//                maxTotal = tempTotal; 
//                index++;
//           }
//        }
//        
//        System.out.println(maxTotal);
//        System.out.println(index);
        
    }
}
