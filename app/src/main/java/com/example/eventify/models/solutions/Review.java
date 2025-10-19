package com.example.eventify.models.solutions;

public class Review {
    private String id;
    private Solution solution;
    private String comment;
    private int grade;
    private String status;
    private String authorId;

    public Review() {
    }

    public Review(String id, Solution solution, String comment, int grade, String status, String authorId) {
        this.id = id;
        this.solution = solution;
        this.comment = comment;
        this.grade = grade;
        this.status = status;
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}

