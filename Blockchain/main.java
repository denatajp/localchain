package Blockchain;

import java.util.List;

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

    public static void main(String[] args) {
        List<List<Transaction>> trx = Inisialisasi.inisialisasi(8);
        System.out.println("Transaksi pertama : \n" + trx.get(0));
        System.out.println("");
        System.out.println("Transaksi kedua : \n" + trx.get(1));
        System.out.println("");
        System.out.println("Transaksi ketiga : \n" + trx.get(2));
        System.out.println("");
        System.out.println("Transaksi keempat : \n" + trx.get(3));
        System.out.println("");
        System.out.println("Transaksi kelima : \n" + trx.get(4));

        int indexTerbaik = getBestTranx(trx);
        System.out.println("Index terbaik : " + indexTerbaik);
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
