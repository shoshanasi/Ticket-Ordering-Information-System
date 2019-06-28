
package test;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Admin
 */
@ManagedBean(name = "runTesters")
@ViewScoped
public class RunTesters {
    
    private int numOfThreads;
    
    public RunTesters(){}
    
    public RunTesters(int numOfThreads) {
        this.numOfThreads = numOfThreads;
    }
    
    public void runTests() {
        Thread tr;
        for(int i = 0; i < numOfThreads; i++) {
            tr = new Thread(new Tester(i));
            tr.start();            
        }
    }

    /**
     * @return the numOfThreads
     */
    public int getNumOfThreads() {
        return numOfThreads;
    }

    /**
     * @param numOfThreads the numOfThreads to set
     */
    public void setNumOfThreads(int numOfThreads) {
        this.numOfThreads = numOfThreads;
    }
    
}
