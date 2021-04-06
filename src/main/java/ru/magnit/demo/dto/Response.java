package ru.magnit.demo.dto;

public class Response {

    ResponseStatus status;
    String comment;



    public Response(){}

    public Response(ResponseStatus status, String comment){
        this.status = status;
        this.comment = comment;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
