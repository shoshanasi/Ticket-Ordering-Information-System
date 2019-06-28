
package recommender;

import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;

/**
 * Represents the list of the shows users liked
 * is called when we dont have information about the user or the shows he searches
 */
@ManagedBean(name = "similarityShows")
@SessionScoped
public class BestShows {

    private ArrayList<ShowUserGrades> closeArray = new ArrayList<>();
    private EventsInfoImpl dbInfo;

    //counts the number of threds finished
    private CountDownLatch latch;
    
    
    /**
     * Empty Constructor.
     */
    public BestShows() {
    }
    
    /**
     * Runs on all the shows and gets their total grade
     * @return a list of ShowUserGrades 
     */
    public synchronized ArrayList<ShowUserGrades> start(){
        try {
            if(dbInfo == null)
                dbInfo = new EventsInfoImpl();
            List<Integer> shows = new ArrayList<>();
            shows.addAll((dbInfo).getShowsId());
            latch = new CountDownLatch(shows.size());
            for(int show: shows){
                Thread t1 = new Thread(new BestShows().new RunnableImpl(show, dbInfo, latch)); 
                t1.start();
            }
            latch.await();
        } catch (SQLException ex) {
            Logger.getLogger(BestShows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BestShows.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getCloseArray();
    }
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public ArrayList<ShowUserGrades> getCloseArray() {
        return closeArray;
    }

    public synchronized void addCloseArray(ShowUserGrades num) {
        this.closeArray.add(num);
    }
    
    
    /**
    * Represents the grades of users for a specific show
    * is called for every show and puts the grade in the list of ShowUserGrades
    * when its finished it counts down
    */
    private class RunnableImpl implements Runnable { 
        
        private int show2;
        private final CountDownLatch doneSignal;
        
        @ManagedProperty(value = "#{userBean.dbInfo}")
        private EventsInfoImpl dbInfo;

        /**
        * Constructor For the RunnableImpl.
        * @param show2 a specific show.
        * @param dbInfo database connection 
        * @param doneSignal the count down parameter.
        */
        public RunnableImpl(int show2, EventsInfoImpl dbInfo, CountDownLatch doneSignal) {
            this.show2 = show2;
            dbInfo = dbInfo;
            this.doneSignal = doneSignal;
        }
        
        /**
        * Checks the total grade
        */
        public void run(){
            try {
                double grade = 0;
                if(dbInfo == null)
                    dbInfo = new EventsInfoImpl();
                FilteredRowSet showGrades = dbInfo.getShowRating(show2);
                if(showGrades != null){
                    while(showGrades.next()){
                        if(show2 == showGrades.getInt("show_code")){
                            grade = grade + showGrades.getDouble("grade");
                        }
                    }
                    ShowUserGrades number = new ShowUserGrades("", grade, show2);
                    addCloseArray(number);
                }
                this.doneSignal.countDown();
            } catch (SQLException ex) {
                Logger.getLogger(BestShows.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    }
}
