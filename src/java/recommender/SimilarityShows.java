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
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;



/**
 *
 * 
 */
@ManagedBean(name = "similarityShows")
@SessionScoped
public class SimilarityShows{

    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    private FilteredRowSet show1;
    private List<Integer> shows;
    private ArrayList<ShowUserGrades> closeArray;
    

    private CountDownLatch latch;
    /**
     * Creates a new instance of Similarity
     */
    public SimilarityShows() {
        
    }
    
    
    public ArrayList<ShowUserGrades> getCloseArray() {
        return closeArray;
    }

    public synchronized void addCloseArray(ShowUserGrades num) {
        this.closeArray.add(num);
        latch.countDown();
    }
    
    public ArrayList<ShowUserGrades> showsCheck(FilteredRowSet show1, List<Integer> shows) throws InterruptedException{
        this.show1 = show1;
        this.shows = shows;
        for(int show: shows){
            Thread t1 = new Thread(new SimilarityShows().new RunnableImpl(show)); 
            t1.start(); 
        }
        latch = new CountDownLatch(shows.size());
        latch.await();
        return this.getCloseArray();
    }
    
    
    private class RunnableImpl implements Runnable { 
        
        private int show2;

        public RunnableImpl(int show2) {
            this.show2 = show2;
        }
        
        
        public void run(){
            System.out.println(show2+" start");
            try {
                if(dbInfo instanceof EventsManagerInfoImpl){
                    FilteredRowSet second = ((EventsManagerInfoImpl)dbInfo).getShowRating(show2);
                    double multiNum = 0;
                    double firstNum = 0;
                    double secondNum = 0;

                    while(show1.next()){
                       while(second.next()){
                           if(second.getInt("user_id")>show1.getInt("user_id"))
                               break;
                           else if(second.getInt("user_id") == show1.getInt("user_id")){
                               multiNum = multiNum +show1.getInt("grade")*second.getInt("grade");
                               firstNum = firstNum + Math.pow(show1.getInt("grade"), 2);
                               secondNum = secondNum + Math.pow(second.getInt("grade"), 2);
                           }
                       }
                    }
                    ShowUserGrades num = new ShowUserGrades("", multiNum/(Math.sqrt(secondNum)*Math.sqrt(firstNum)),show2);
                    addCloseArray(num);
                }else{
                    FacesMessage msg = new FacesMessage("you arent a manager therefor acsses denied");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }

            } catch (SQLException ex) {
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
