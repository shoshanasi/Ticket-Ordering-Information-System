
package recommender;

import eventsinformation.EventsInfoImpl;
import eventsinformation.EventsManagerInfoImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.sql.rowset.FilteredRowSet;

/**
 * Runs the different types of recommendations
 */
@Named(value = "genaralSimilarity")
@Dependent
public class GenaralSimilarity {

    private final int RECOMMANDED_NUM = 5;
    private final int RECOMMANDED_CHANGE_NUM = 2;          
    private EventsInfoImpl dbInfo;
    private List<String> imagesList = new ArrayList<>();

    /**
     * Constructor for the GenaralSimilarity.
     * starting the best shows without information about the user or the shows he searches 
     */
    public GenaralSimilarity() {
        try {
            imagesList.clear();
            if(dbInfo == null)
                dbInfo = new EventsInfoImpl();
            int[] shows = bestShowsRecoomand();
            putPhotos(shows);
        } catch (SQLException ex) {
            Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * addes the shows photos to image list
     * @param shows the shows we will recommend.
     */
    public void putPhotos(int[] shows){
        if(!this.imagesList.isEmpty()&& !(this.imagesList.size()<RECOMMANDED_NUM-shows.length)){
            for(int j=0; j<shows.length; j++){
                this.imagesList.remove(j);
            }
        }
        for(int i=0; i<shows.length; i++){
            try {
                FilteredRowSet rs = dbInfo.getShowPictures(shows[i]);
                rs.beforeFirst();
                if(rs.next() && !this.imagesList.contains(rs.getString("pic"))){
                    this.imagesList.add(rs.getString("pic"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * addes the shows photos to image list
     * @return the list of recommended shows
     */
    public int[] bestShowsRecoomand(){
        int[] recommand = new int[RECOMMANDED_NUM];
        BestShows best = new BestShows();
        ArrayList<ShowUserGrades> bestOnes = best.start();
        if(bestOnes == null){
            recommand = fillIn(RECOMMANDED_NUM);
            return recommand;
        }  
        Collections.sort(bestOnes);
        if(bestOnes.size()<RECOMMANDED_NUM){
            recommand = fillIn(RECOMMANDED_NUM-bestOnes.size());
            for(int i=RECOMMANDED_NUM-bestOnes.size(); i<RECOMMANDED_NUM; i++)
                recommand[i] = bestOnes.get(i).getShowCode();
            return recommand;
        }
        for(int i=0; i<RECOMMANDED_NUM; i++)
            recommand[i] = bestOnes.get(i).getShowCode();
        return recommand;
    }
    
    /**
    * Represents the likens of a user to the other users according to the shows they liked
    * is called when the manager wants to have recommendations for all users
    */
    public void usersSimilarity() {
        try {
            List<String> users;
            users = dbInfo.getUsersId();
            for(String user1: users){
                SimilarityUsers object = new SimilarityUsers();
                object.defineParmters(user1, users);
                object.start();
            }
        } catch (SQLException ex) {
            Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * gets the most likely shows the user would like
    * @param userId the user we want the information on
    */
    public void userRecommadsion(String userId){
        int[] recommand = new int[RECOMMANDED_NUM];
        try {
            FilteredRowSet userList = dbInfo.getUserRecommandsion(userId);
            if(userList == null){
                recommand = fillIn(RECOMMANDED_NUM);
                putPhotos(recommand);
                return;
            }  
            if(userList.size()<RECOMMANDED_NUM){
                recommand = fillIn(RECOMMANDED_NUM-userList.size());
                for(int i=RECOMMANDED_NUM-userList.size(); i<RECOMMANDED_NUM; i++){
                    userList.next();
                    recommand[i] = userList.getInt("show_code");
                }
                putPhotos(recommand);
                return;
            }
            for(int i=0; i<RECOMMANDED_NUM; i++){
                userList.next();
                recommand[i] = userList.getInt("show_code");
            }
        } catch (SQLException ex) {
            Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        putPhotos(recommand);
    }
    
    /**
    * Represents the likens of a show to the other shows according to the users that liked it
    * is called when the user selects a show
    * @param show the show we are looking for
    */
    public void showsSimilarity(int show){
        int[] recommand = new int[RECOMMANDED_CHANGE_NUM];
        try {
            List<Integer> shows;
            shows = (dbInfo).getShowsId();
            FilteredRowSet first = (dbInfo).getShowRating(show);
            SimilarityShows similerList = new SimilarityShows();
            ArrayList<ShowUserGrades> similerShows;
            similerShows = similerList.showsCheck(first, shows);

            if(similerShows == null){
                recommand = fillIn(RECOMMANDED_CHANGE_NUM);
                putPhotos(recommand);
                return;
            }  
            
            Collections.sort(similerShows);
            
            if(similerShows.size()<RECOMMANDED_CHANGE_NUM){
                recommand = fillIn(RECOMMANDED_CHANGE_NUM-similerShows.size());
                int numberWanted = RECOMMANDED_CHANGE_NUM-similerShows.size();
                for(int i=numberWanted; i<RECOMMANDED_CHANGE_NUM; i++){
                    recommand[i] = similerShows.get(i-numberWanted).getShowCode();
                }
                putPhotos(recommand);
                return;
            }
            

            for(int i=0; i<RECOMMANDED_NUM; i++)
                recommand[i] = similerShows.get(i).getShowCode();
        } catch (SQLException ex) {
            Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        putPhotos(recommand);
    }
    
    /**
     * fills in randomly because we cant find recommendations
     * @param requstedNum the amount we didnt find
     * @return the list of recommended shows
     */
    public int[] fillIn(int requstedNum){
        int[] recommand = new int[requstedNum];
        try {
            List<Integer> shows;
            shows = (dbInfo).getShowsId();
            if(shows.isEmpty()){
                for(int i=0; i<requstedNum; i++)
                    recommand[i] = -1;
                return recommand;
            }
            int minimum = Math.min(requstedNum, shows.size());
            for(int i=0; i<minimum; i++){
                recommand[i] = shows.get(i);
            }
        } catch (SQLException ex) {
            Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return recommand;
    }
    
    
    public List<String> getImagesList() {
        return imagesList;
    }

    public void setImagesList(List<String> imagesList) {
        this.imagesList = imagesList;
    }
    
    
    public void setDbInfo(EventsInfoImpl dbInfo) {
        this.dbInfo = dbInfo;
    }
    
    
    
}
