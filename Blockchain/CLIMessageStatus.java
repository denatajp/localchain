package Blockchain;

import core.SimClock;
import core.SimScenario;

public class CLIMessageStatus {

    public static boolean startGenerate = false;
    public static boolean doneGenerate = false;
    public static boolean doneGrouping = false;
    public static boolean doneMiningVerification = false;
    public static boolean doneStoring = false;
    public static boolean doneSelectionAppending = false;
    
    // ANSI escape codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String MAGENTA = "\u001B[35m";

    public static final SimScenario inst = SimScenario.getInstance();
    public static int difficulty = inst.getDifficulty();
    public static int minersPerArea = inst.getMinersInGroup();
    public static int maxTrx;
    public static boolean isGuiMode = true;
    
    
    public static void setGuiMode(boolean b) {
        isGuiMode = b;
    }
    
    public static void setMaxTrx(int a) {
        maxTrx = a;
    }
    
    public static void start() {
        String block = RED
                + "$$$$$$$\\ $$\\                  $$\\       \n" + RED
                + "$$  __$$\\$$ |                 $$ |      \n" + RED
                + "$$ |  $$ $$ |$$$$$$\\  $$$$$$$\\$$ |  $$\\ \n" + RED
                + "$$$$$$$\\ $$ $$  __$$\\$$  _____$$ | $$  |\n" + RED
                + "$$  __$$\\$$ $$ /  $$ $$ /     $$$$$$  / \n" + RED
                + "$$ |  $$ $$ $$ |  $$ $$ |     $$  _$$<  \n" + RED
                + "$$$$$$$  $$ \\$$$$$$  \\$$$$$$$\\$$ | \\$$\\ \n" + RED
                + "\\_______/\\__|\\______/ \\_______\\__|  \\__|\n" + RED
                + "                                        " + RESET;

        String chain = YELLOW
                + "         $$\\               $$\\          \n" + YELLOW
                + "         $$ |              \\__|         \n" + YELLOW
                + " $$$$$$$\\$$$$$$$\\  $$$$$$\\ $$\\$$$$$$$\\  \n" + YELLOW
                + "$$  _____$$  __$$\\ \\____$$\\$$ $$  __$$\\ \n" + YELLOW
                + "$$ /     $$ |  $$ |$$$$$$$ $$ $$ |  $$ |\n" + YELLOW
                + "$$ |     $$ |  $$ $$  __$$ $$ $$ |  $$ |\n" + YELLOW
                + "\\$$$$$$$\\$$ |  $$ \\$$$$$$$ $$ $$ |  $$ |\n" + YELLOW
                + " \\_______\\__|  \\__|\\_______\\__\\__|  \\__|\n" + GREEN
                + "                                        \n" + YELLOW
                + "-------------------------------------------"
                + "                                        ";

        System.out.println(block);
        System.out.println(chain);
        System.out.println(CYAN + "AUTHOR\t\t: Denata");
        System.out.println(YELLOW + "-------------------------------------------");
        System.out.println(CYAN + "Difficulty\t\t: " + difficulty);
        System.out.println(CYAN + "Miner per area\t\t: " + minersPerArea);
        System.out.println(CYAN + "Max trx\t\t\t: " + maxTrx);
        System.out.println(CYAN + "Mode (GUI/Terminal)\t: " + (isGuiMode ? "GUI" : "Terminal"));
        System.out.println(YELLOW + "-------------------------------------------");
    }

    public static void statusStartGenerate() {
        if (!startGenerate) {
            System.out.println("");
            System.out.println(GREEN + "=================================================" + RESET);
            System.out.println(GREEN + "FASE 1 : Pembangkitan Transaksi         " + RESET);
            System.out.println(GREEN + "-------------------------------------------------" + RESET);
            System.out.println("Semua node miner di area akan membuat transaksi         ");
            System.out.println("Membangkitkan transaksi........         ");
            startGenerate = true;
        }
    }

    public static void statusDoneGenerate() {
        if (!doneGenerate) {
            System.out.println("Selesai membangkitkan!");
            System.out.println(GREEN + "=================================================");
            System.out.println("");
            System.out.println(GREEN + "=================================================");
            System.out.println(GREEN + "FASE 2 : Grouping Transaksi         ");
            System.out.println(GREEN + "-------------------------------------------------");
            System.out.println("Mengelompokkan semua transaksi....");
            doneGenerate = true;
        }
    }

    public static void statusDoneGrouping() {
        if (!doneGrouping) {
            System.out.println("Selesai Grouping!");
            System.out.println(GREEN + "=================================================");
            System.out.println("");
            System.out.println(GREEN + "=================================================");
            System.out.println(GREEN + "FASE 3 : Mining & Verification         ");
            System.out.println(GREEN + "-------------------------------------------------");
            System.out.println("Memulai proses mining & verification.....");
            doneGrouping = true;
        }
    }

    public static void statusDoneMiningVerification() {
        if (!doneMiningVerification) {
            System.out.println(".............");
            System.out.println("......................");
            System.out.println("Selesai Mining & Verification!!");
            System.out.println(GREEN + "=================================================");
            System.out.println("");
            System.out.println(GREEN + "=================================================");
            System.out.println(GREEN + "FASE 4 : Storing         ");
            System.out.println(GREEN + "-------------------------------------------------");
            System.out.println("Memulai proses storing.....");
            doneMiningVerification = true;
        }
    }

    public static void statusDoneStoring() {
        if (!doneStoring) {
            System.out.println("Semua localchain sudah berada di Home!!");
            System.out.println(GREEN + "=================================================");
            System.out.println("");
            System.out.println(GREEN + "=================================================");
            System.out.println(GREEN + "FASE 5 : Selection & Appending         ");
            System.out.println(GREEN + "-------------------------------------------------");
            System.out.println("Memilih Localchain terbaik.....");
            doneStoring = true;
        }
    }

    public static void statusDoneSelectionAppending() {
        if (!doneSelectionAppending) {
            System.out.println("SEMUA TRANSAKSI SUDAH DITAMBAHKAN DI BLOCKCHAIN");
            System.out.println("Waktu proses : " + SimClock.getTime() + " s");
            System.out.println(GREEN + "=================================================");
            System.out.println("");
            System.out.println(GREEN + "=================================================");
            System.out.println(GREEN + "FASE 6 : Reward         ");
            System.out.println(GREEN + "-------------------------------------------------");
            System.out.println("Memberikan reward ke para miner.....");
            doneSelectionAppending = true;
        }
    }

    public static void done() {
        System.out.println("-------------------------------------------------");
        System.out.println("Selesai memberikan reward ke semua miner...");
        System.out.println("MEKANISME TRANSAKSI SELESAI!");
        System.out.println("Waktu proses : " + SimClock.getTime() + " s");
        System.out.println(GREEN + "=================================================");
    }
}
