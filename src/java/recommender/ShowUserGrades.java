
package recommender;


import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Represents the object that keeps the information while checking user/show likens
 * 
 */
@ManagedBean(name = "similarityShows")
@SessionScoped
public class ShowUserGrades implements Comparable< ShowUserGrades >{

    private String userCode;
    private int showCode;
    private double grade;
    
    /**
    * Constructor For the RunnableImpl.
    * @param userCode the user id.
    * @param grade the grade the user gave
    * @param showCode the show.
    */
    public ShowUserGrades(String userCode, double grade, int showCode) {
        this.userCode = userCode;
        this.showCode = showCode;
        this.grade = grade;
    }
    
    /**
     * Empty Constructor.
     */
    public ShowUserGrades() {
    }
    
    /**
     * used for Collections.sort of lists of this object.
     */
    @Override
    public int compareTo(ShowUserGrades t) {
        if(t.getGrade()<=this.getGrade())
            return 1;
        return 0;
    }
    
    public int getShowCode() {
        return showCode;
    }

    public void setShowCode(int showCode) {
        this.showCode = showCode;
    }
    
    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }
    
    
    
}
