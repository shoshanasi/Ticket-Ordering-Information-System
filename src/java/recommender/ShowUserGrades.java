/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Shani
 */
@ManagedBean(name = "similarityShows")
@SessionScoped
public class ShowUserGrades implements Comparable< ShowUserGrades >{

    
    private String userCode;
    private int showCode;

    private double grade;
    
    public ShowUserGrades(String userCode, double grade, int showCode) {
        this.userCode = userCode;
        this.showCode = showCode;
        this.grade = grade;
    }
    /**
     * Creates a new instance of ShowGrades
     */
    public ShowUserGrades() {
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
    
    @Override
    public int compareTo(ShowUserGrades t) {
        if(t.getGrade()<=this.getGrade())
            return 1;
        return 0;
    }

    
    
}
