
package recommender;

import newuserbean.*;
import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
 * Represents the likens of a user to the other users according to the shows they liked
 * then computes the likens of a show the user didnt grade
 * is called when the manager wants to have recommendations for all users
 */
@ManagedBean(name = "similarityUsers")
@SessionScoped
public class SimilarityUsers extends Thread{

    
    private EventsInfoImpl dbInfo;
    
    String user;
    List<String> users;
    
    private ArrayList<ShowUserGrades> closeArray = new ArrayList<>();
    
    //counts the number of threds finished
    CountDownLatch latch;
    
    /**
     * Creates a new instance of Similarity
     */
    public SimilarityUsers() {
        try {
            if(dbInfo == null)
                dbInfo = new EventsInfoImpl();
        } catch (SQLException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param user the user we checking
     * @param users the users to check with 
     */
    public void defineParmters(String user, List<String> users){
        this.user = user;
        this.users = users;
    }
    
    /**
     * runs for every user the likens to the specific user by using different threads
     * waits for all to finish and then calculates the likens of every show the specific user didnt grade
     */
    public void run(){
        try {
            System.out.println(user+" start");
            latch = new CountDownLatch(users.size());
            for(String user2: users){
                Thread t1;
                t1 = new Thread(new SimilarityUsers().new RunnableImpl(user ,user2, latch));
                t1.start();
            }
            System.out.println(user+" finish");
            latch.await();
            List<Integer> shows;
            shows = (dbInfo).getShowsId();
            FilteredRowSet showsOfUser;
            showsOfUser = (dbInfo).getUserRating(user);
            List<Integer> columnList = new ArrayList<>();
            showsOfUser.beforeFirst();
            while(showsOfUser.next()) {
                columnList.add(showsOfUser.getInt("show_code"));
            }
            shows.removeAll((Collection<?>) columnList);
            
            if(closeArray != null && !closeArray.isEmpty()){
                double num = closeArray.size();
                for(int show: shows){
                    double grade = 0;
                    FilteredRowSet first = (dbInfo).getShowRating(show);
                    while(first.next()){
                        String id = first.getString("user_id");
                        for(ShowUserGrades usersGrade: closeArray){
                            if(id.equals(usersGrade.getUserCode())){
                                grade = grade + usersGrade.getGrade();
                            }
                        }
                    }
                    (dbInfo).insertReccomd(show, user, grade/num);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public ArrayList<ShowUserGrades> getCloseArray() {
        return closeArray;
    }

    public synchronized void addCloseArray(ShowUserGrades num) {
        this.closeArray.add(num);
    }
    
    
    /**
    * Represents the similarity between 2 specific users
    * is called for every 2 users and puts the grade in the list of ShowUserGrades
    * when its finished it counts down
    */
    private class RunnableImpl implements Runnable {

        private String user1;
        private String user2;
        private final CountDownLatch doneSignal;
        
        /**
        * Constructor For the RunnableImpl.
        * @param user1 a specific user.
        * @param user2 a specific user 
        * @param doneSignal the count down parameter.
        */
        public RunnableImpl(String user1, String user2, CountDownLatch doneSignal) {
            this.user1 = user1;
            this.user2 = user2;
            this.doneSignal = doneSignal;
        }
        
        /**
        * Checks the similarity between the users
        */
        @Override
        public void run() {
            try {
                FilteredRowSet first = (dbInfo).getUserRating(user1);
                FilteredRowSet second = (dbInfo).getUserRating(user2);
                double multiNum = 0;
                double firstNum = 0;
                double secondNum = 0;

                while(first.next()){
                   while(second.next()){
                       if(second.getInt("show_code")>first.getInt("show_code"))
                           break;
                       else if(second.getInt("show_code") == first.getInt("show_code")){
                           multiNum = multiNum + first.getInt("grade")*second.getInt("grade");
                       }
                       firstNum = firstNum + Math.pow(first.getInt("grade"), 2);
                       secondNum = secondNum + Math.pow(second.getInt("grade"), 2);
                   }
                }
                ShowUserGrades num = new ShowUserGrades(user2, multiNum/(Math.sqrt(secondNum)*Math.sqrt(firstNum)), 0);
                addCloseArray(num);
                this.doneSignal.countDown();
            } catch (SQLException ex) {
                FacesMessage msg = new FacesMessage(ex.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        
    
        }
        
    }
    
    /*public double similarUsers(String user1, String user2){
        try {
            if(dbInfo instanceof EventsManagerInfoImpl){
                FilteredRowSet first = ((EventsManagerInfoImpl)dbInfo).getUserRating(user1);
                FilteredRowSet second = ((EventsManagerInfoImpl)dbInfo).getUserRating(user2);
                double multiNum = 0;
                double firstNum = 0;
                double secondNum = 0;

                while(first.next()){
                   while(second.next()){
                       if(second.getInt("show_code")>first.getInt("show_code"))
                           break;
                       else if(second.getInt("show_code") == first.getInt("show_code")){
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
