package com.shubhodip.nutrisift.user;

public class SecurityAnswerRequest {
    private String email;
    private String answer;
    
    // Default constructor for Jackson
    public SecurityAnswerRequest() {
    }
    
    public SecurityAnswerRequest(String email, String answer) {
        this.email = email;
        this.answer = answer;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}