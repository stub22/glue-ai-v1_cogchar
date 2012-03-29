package org.cogchar.bind.cogbot.main;

// This class contains a response string and an integer confidence level

import java.util.List;

public class GenRespWithConf
{
    private String myResponse;
    private int myConfidence;

    private String myServiceName;
    public String getServiceName(){
        return myServiceName;
    }

    private Long myStartTimeMillis=null;
    public Long getStartTimeMillis() {
        return myStartTimeMillis;
    }

    private Long myEndTimeMillis=null;
    public Long getEndTimeMillis() {
        return myEndTimeMillis;
    }

    public Long getElapsedTimeMillis() {
        if(myStartTimeMillis==null || myEndTimeMillis==null) {
            return null;
        }
        return myEndTimeMillis-myStartTimeMillis;
    }

    public GenRespWithConf(){
        myResponse = "";
        myConfidence = 0;
    }

    public GenRespWithConf(String response,int confidence) {
        myResponse = response.trim();
		if(myResponse.isEmpty()){
			myConfidence = -1;
		}else{
			myConfidence = confidence;
		}
	}

    public String getResponse() {
        return myResponse;
    }

    public void setResponse(String resp) {
        myResponse = resp;
    }

    public int getConfidence() {
        return myConfidence;
    }

    public void setConfidence(int conf) {
        myConfidence = conf;
    }

    void setServiceUsed(String serviceName) {
        myServiceName=serviceName;
    }

    void setElapsedTimeDetails(long startTime, long endTime) {
        if(startTime>endTime){
            throw new IllegalArgumentException("end time must be >= start time");
        }
        myStartTimeMillis=startTime;
        myEndTimeMillis=endTime;
    }
	
    public static GenRespWithConf getBest(List<GenRespWithConf> askResponses){
        GenRespWithConf best = null;
        for(GenRespWithConf re : askResponses){
            if(best == null || re.getConfidence() > best.getConfidence()){
                best = re;
            }
        }
        if(best == null){
            best = new GenRespWithConf("", -1);
        }
        return best;
    }	
}