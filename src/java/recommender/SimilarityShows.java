
package recommender;

import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;



/**
 * Represents the likens of a show to the other shows according to users likes users
 * is called when we have information about the show he searches
 */
@ManagedBean(name = "similarityShows")
@SessionScoped
public class SimilarityShows{

    private EventsInfoImpl dbInfo;
    private FilteredRowSet show1;
    private List<Integer> shows;
    private ArrayList<ShowUserGrades> closeArray = new ArrayList<>();
    
    //counts the number of threds finished
    private CountDownLatch latch;
    
    
    /**
     * Empty Constructor 
     * .
     */
    public SimilarityShows() {
        try {
            if(dbInfo == null)
                dbInfo = new EventsInfoImpl();
        } catch (SQLException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * runs for every show the likens to the specific show by using the threads
     * @param show1 the rating of the show of all users
     * @param shows the shows to check with
     * @return a list of the similarity of the other shows
     */
    public ArrayList<ShowUserGrades> showsCheck(FilteredRowSet show1, List<Integer> shows) throws InterruptedException{
        this.show1 = show1;
        this.shows = shows;
        latch = new CountDownLatch(shows.size());
        for(int show: shows){
            Thread t1 = new Thread(new SimilarityShows().new RunnableImpl(show, latch, show1)); 
            t1.start(); 
        }
        latch.await();
        return this.getCloseArray();
    }
    
    public ArrayList<ShowUserGrades> getCloseArray() {
        return closeArray;
    }

    public synchronized void addCloseArray(ShowUserGrades num) {
        this.closeArray.add(num);
    }
    
    
    /**
    * Represents the similarity between 2 specific shows
    * is called for every 2 shows and puts the grade in the list of ShowUserGrades
    * when its finished it counts down
    */
    private class RunnableImpl implements Runnable { 
        
        private int show2;
        private final CountDownLatch doneSignal;
        private FilteredRowSet show1;
        
        /**
        * Constructor For the RunnableImpl.
        * @param show2 the show we checking with.
        * @param show1 the likes of the specific show
        * @param doneSignal the count down parameter.
        */
        public RunnableImpl(int show2, CountDownLatch doneSignal, FilteredRowSet show1) {
            this.show2 = show2;
            this.doneSignal = doneSignal;
            this.show1 = show1;
        }
        
        /**
        * Checks the similarity between the shows
        */
        public void run(){
            System.out.println(show2+" start");
            try {
                FilteredRowSet second = (dbInfo).getShowRating(show2);
                double multiNum = 0;
                double firstNum = 0;
                double secondNum = 0;

                if(show1 != null && second != null && show1.size() != 0 && second.size() != 0){
                    show1.beforeFirst();
                    while(show1.next()){
                        second.beforeFirst();
                       while(second.next()){
                           if(((second.getString("user_id")).compareTo(show1.getString("user_id"))) > 0)
                               break;
                           else if(second.getString("user_id").compareTo(show1.getString("user_id")) == 0){
                               multiNum = multiNum +show1.getInt("grade")*second.getInt("grade");
                           }
                           firstNum = firstNum + Math.pow(show1.getInt("grade"), 2);
                           secondNum = secondNum + Math.pow(second.getInt("grade"), 2);
                       }
                    }
                    ShowUserGrades num = new ShowUserGrades("", multiNum/(Math.sqrt(secondNum)*Math.sqrt(firstNum)),show2);
                    addCloseArray(num);
                }
                
                this.doneSignal.countDown();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                FacesMessage msg = new FacesMessage(ex.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }

            System.out.println(show2+" finish");
        }
        
        /*public double similarShows(String show1, String show2){
            try {
                if(dbInfo instanceof EventsManagerInfoImpl){
                    FilteredRowSet first = ((EventsManagerInfoImpl)dbInfo).getShowRating(show1);
                    FilteredRowSet second = ((EventsManagerInfoImpl)dbInfo).getShowRating(show2);
                    double multiNum = 0;
                    double firstNum = 0;
                    double secondNum = 0;

                    while(first.next()){
                       while(second.next()){
                           if(second.getInt("user_id")>first.getInt("user_id"))
                               break;
                           else if(second.getInt("user_id") == first.getInt("user_id")){
                               multiNum = multiNum + first.getInt("grade")*second.getInt("grade");
                               firstNum = firstNum + Math.pow(first.getInt("grade"), 2);
                               secondNum = secondNum + Math.pow(second.getInt("grade"), 2);
                           }
                       }
                    }
                    return multiNum/(Math.sqrt(secondNum)*Math.sqrt(firstNum));
                }else{
                    FacesMessage msg = new FacesMessage("you arent a manager therefor acsses denied");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return 0;
                }

            } catch (SQLException ex) {
                FacesMessage msg = new FacesMessage(ex.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return 0;
            }

        }**/

    }
    
    
    
    
    
}
