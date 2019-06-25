/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * 
 */
@ManagedBean(name = "similarityUsers")
@SessionScoped
public class SimilarityUsers extends Thread{

    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    String user;
    List<String> users;
    
    private ArrayList<ShowUserGrades> closeArray;
    

    CountDownLatch latch;
    /**
     * Creates a new instance of Similarity
     */
    public SimilarityUsers() {
        
    }
    
    public ArrayList<ShowUserGrades> getCloseArray() {
        return closeArray;
    }

    public synchronized void addCloseArray(ShowUserGrades num) {
        this.closeArray.add(num);
        latch.countDown();
    }
    
    public void defineParmters(String user, List<String> users){
        this.user = user;
        this.users = users;
    }
    
    public void run(){
        try {
            System.out.println(user+" start");
            for(String user2: users){
                Thread t1;
                t1 = new Thread(new SimilarityUsers().new RunnableImpl(user ,user2));
                t1.start();
            }
            System.out.println(user+" finish");
            latch = new CountDownLatch(users.size());
            
            latch.await();
            
            List<Integer> shows;
            shows = ((EventsManagerInfoImpl)dbInfo).getShowsId();
            FilteredRowSet showsOfUser;
            showsOfUser = ((EventsManagerInfoImpl)dbInfo).getUserRating(user);
            List<Integer> columnList = new ArrayList<>();
            showsOfUser.beforeFirst();
            while(showsOfUser.next()) {
                columnList.add(showsOfUser.getInt("show_code"));
            }
            shows.removeAll((Collection<?>) showsOfUser);
            
            double grade = 0;
            for(int show: shows){
                FilteredRowSet first = ((EventsManagerInfoImpl)dbInfo).getShowRating(show);
                while(first.next()){
                    String id = first.getString("user_id");
                    for(ShowUserGrades usersGrade: closeArray){
                        if(id.equals(usersGrade.getUserCode()))
                            grade = grade + usersGrade.getGrade();
                    }
                }
                ((EventsManagerInfoImpl)dbInfo).insertReccomd(show, user, grade);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class RunnableImpl implements Runnable {

        private String user1;
        private String user2;

        public RunnableImpl(String user1, String user2) {
            this.user1 = user1;
            this.user2 = user2;
        }
        @Override
        public void run() {
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
                    ShowUserGrades num = new ShowUserGrades(user2, multiNum/(Math.sqrt(secondNum)*Math.sqrt(firstNum)), 0);
                    addCloseArray(num);
                }else{
                    FacesMessage msg = new FacesMessage("you arent a manager therefor acsses denied");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }

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
