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
import javax.faces.bean.ManagedProperty;
import javax.sql.rowset.FilteredRowSet;

/**
 *
 * @author Shani
 */
@Named(value = "genaralSimilarity")
@Dependent
public class GenaralSimilarity {

    
    @ManagedProperty(value = "#{userBean.dbInfo}")
    private EventsInfoImpl dbInfo;
    
    private List<String> imagesList = new ArrayList<>();

    
    /**
     * Creates a new instance of UnionIntersection
     */
    public GenaralSimilarity() {
        imagesList.clear();
        int[] shows = bestShowsRecoomand(5);
        for(int i=0; i<5; i++){
            try {
                FilteredRowSet rs = null;
                rs = dbInfo.getShowPictures(shows[i]);
                rs.beforeFirst();
                while(rs.next()){
                    this.imagesList.add(rs.getString("pic"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public int[] bestShowsRecoomand(int requstedNum){
        int[] recommand = new int[requstedNum];
        BestShows best = new BestShows();
        ArrayList<ShowUserGrades> bestOnes = best.start();
        Collections.sort(bestOnes);
        for(int i=0; i<requstedNum; i++)
            recommand[i] = bestOnes.get(i).getShowCode();
        return recommand;
    }
    
    public void usersSimilarity() {
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                List<String> users;
                users = ((EventsManagerInfoImpl)dbInfo).getUsersId();
                for(String user1: users){
                    SimilarityUsers object = new SimilarityUsers();
                    object.defineParmters(user1, users);
                    object.start();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    public int[] showsSimilarity(int show, int requstedNum){
        int[] recommand = new int[requstedNum];
        if(dbInfo instanceof EventsManagerInfoImpl){
            try {
                List<Integer> shows;
                shows = ((EventsManagerInfoImpl)dbInfo).getShowsId();
                FilteredRowSet first = ((EventsManagerInfoImpl)dbInfo).getShowRating(show);
                SimilarityShows similerList = new SimilarityShows();
                ArrayList<ShowUserGrades> similerShows;
                similerShows = similerList.showsCheck(first, shows);
                
                Collections.sort(similerShows);
                
                for(int i=0; i<requstedNum; i++)
                    recommand[i] = similerShows.get(i).getShowCode();
            } catch (SQLException ex) {
                Logger.getLogger(SimilarityUsers.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(GenaralSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return recommand;
    }
    
    
    public List<String> getImagesList() {
        return imagesList;
    }

    public void setImagesList(List<String> imagesList) {
        this.imagesList = imagesList;
    }
    
    
}
