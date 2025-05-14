package report;

import core.DTNHost;
import core.UpdateListener;
import java.util.List;

public class StorageReport extends Report implements UpdateListener {

    public StorageReport(){
        super();
    }
    
    @Override
    public void updated(List<DTNHost> hosts) {
        
    }
    
    @Override
    public void done() {
        System.out.println("Masuk done la");
        write("Masuk done la ");
    }
}
