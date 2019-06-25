/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author Shani
 */
@ManagedBean(name = "similarityShows")
@SessionScoped
public class BestShows {

    
    private ArrayList<ShowUserGrades> closeArray;

    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;

    
    private CountDownLatch latch;
    /**
     * Creates a new instance of BestShows
     */
    public BestShows() {
    }
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    public ArrayList<ShowUserGrades> getCloseArray() {
        return closeArray;
    }

    public synchronized void addCloseArray(ShowUserGrades num) {
        this.closeArray.add(num);
        latch.countDown();
    }
    
    public ArrayList<ShowUserGrades> start(){
        try {
            if(dbInfo == null)
                dbInfo = new EventsInfoImpl();
            List<Integer> shows = new ArrayList<>();
            shows.addAll((dbInfo).getShowsId());
            for(int show: shows){
                Thread t1 = new Thread(new BestShows().new RunnableImpl(show)); 
                t1.start();
            }
            latch = new CountDownLatch(shows.size());
            latch.await();
        } catch (SQLException ex) {
            Logger.getLogger(BestShows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BestShows.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getCloseArray();
    }
    
    private class RunnableImpl implements Runnable { 
        
        private int show2;

        public RunnableImpl(int show2) {
            this.show2 = show2;
        }
        
        
        public void run(){
            try {
                double grade = 0;
                FilteredRowSet showGrades = ((EventsManagerInfoImpl)dbInfo).getShowRating(show2);
                while(showGrades.next()){
                    if(show2 == showGrades.getInt("show_code"))
                        grade = grade + showGrades.getInt("grade");
                }
                ShowUserGrades num = new ShowUserGrades("", grade, show2);
                addCloseArray(num);
            } catch (SQLException ex) {
                Logger.getLogger(BestShows.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    }
}
