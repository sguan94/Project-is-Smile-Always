package io.ppmtool.exceptions;

public class BacklogNotFoundExceptionResponse {
    private String PROJECTNOTFOUND;

    public BacklogNotFoundExceptionResponse(String s){
        this.PROJECTNOTFOUND = s;
    }

    public String getPROJECTNOTFOUND() {
        return PROJECTNOTFOUND;
    }

    public void setPROJECTNOTFOUND(String s) {
        this.PROJECTNOTFOUND = s;
    }
}
