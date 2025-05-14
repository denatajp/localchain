package report;

import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

/**
 * Report mencatat penggunaan storage pada Operator Proxy tiap areanya per
 * sekian waktu
 * @author Denata
 */
public class StorageCapacityReport extends Report implements UpdateListener {

    /**
     * Settings untuk interval waktu tiap berapa detik report akan dicatat.
     * -setting id ({@value})
     */
    public static final String STORAGE_REPORT_INTERVAL = "storageInterval";
    
    /**
     * Nilai default untuk storageInterval
     */
    public static final int DEFAULT_STORAGE_REPORT_INTERVAL = 180;

    private double lastRecord = Double.MIN_VALUE;
    
    /**
     * Interval waktu pencatatan storage usage
     */
    private int interval;
    
    /**
     * Map yang menyimpan penggunaan storage tiap OP per selang waktu
     */
    private Map<DTNHost, List<Integer>> usageStorage = new HashMap<>();

    public StorageCapacityReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(STORAGE_REPORT_INTERVAL)) {
            interval = settings.getInt(STORAGE_REPORT_INTERVAL);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_STORAGE_REPORT_INTERVAL;
        }
    }

    public void updated(List<DTNHost> hosts) {
        if (isWarmup()) {
            return;
        }
        if (SimClock.getTime() - lastRecord >= interval) {

            lastRecord = SimClock.getTime();
            for (DTNHost ho : hosts) {

                if (ho.getName().startsWith("ope8")) {
                    if (usageStorage.containsKey(ho)) {
                        usageStorage.get(ho).add(ho.getStorage());
                    } else {
                        List<Integer> temp = new ArrayList<>();
                        temp.add(ho.getStorage());
                        usageStorage.put(ho, temp);
                    }
                }
            }
        }

    }

    public void done() {
        for (Map.Entry<DTNHost, List<Integer>> entry : usageStorage.entrySet()) {
            DTNHost host = entry.getKey();
            List<Integer> temp = entry.getValue();
            String output = host + " " + temp;
            write(output);
        }

        super.done();
    }
}
