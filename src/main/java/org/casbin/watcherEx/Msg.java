package org.casbin.watcherEx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.casbin.jcasbin.persist.WatcherEx;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


public class Msg implements Serializable {
    private WatcherEx.UpdateType method;
    private String id;
    private String sec;
    private String ptype;
    private String[] oldRule;
    private List<List<String>> oldRules;
    private String[] newRule;
    private List<List<String>> newRules;
    private int fieldIndex;
    private String[] fieldValues;


    public WatcherEx.UpdateType getMethod() {
        return method;
    }

    public void setMethod(WatcherEx.UpdateType method) {
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSec() {
        return sec;
    }

    public void setSec(String sec) {
        this.sec = sec;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String[] getOldRule() {
        return oldRule;
    }

    public void setOldRule(String[] oldRule) {
        this.oldRule = oldRule;
    }

    public List<List<String>> getOldRules() {
        return oldRules;
    }

    public void setOldRules(List<List<String>> oldRules) {
        this.oldRules = oldRules;
    }

    public String[] getNewRule() {
        return newRule;
    }

    public void setNewRule(String[] newRule) {
        this.newRule = newRule;
    }

    public List<List<String>> getNewRules() {
        return newRules;
    }

    public void setNewRules(List<List<String>> newRules) {
        this.newRules = newRules;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public String[] getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String[] fieldValues) {
        this.fieldValues = fieldValues;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "method=" + method +
                ", id='" + id + '\'' +
                ", sec='" + sec + '\'' +
                ", ptype='" + ptype + '\'' +
                ", oldRule=" + Arrays.toString(oldRule) +
                ", oldRules=" + oldRules +
                ", newRule=" + Arrays.toString(newRule) +
                ", newRules=" + newRules +
                ", fieldIndex=" + fieldIndex +
                ", fieldValues=" + Arrays.toString(fieldValues) +
                '}';
    }

    public byte[] marshalBinary() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsBytes(this);
    }

    /**
     * UnmarshalBinary decodes the struct into a User
     */
    public void unmarshalBinary(byte[] data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Msg msg = null;
        try {
            msg = objectMapper.readValue(data, Msg.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initMsg(msg);
    }
    private void initMsg(Msg msg) {
        this.method = msg.method;
        this.id = msg.id;
        this.sec = msg.sec;
        this.ptype = msg.ptype;
        this.oldRule = msg.oldRule;
        this.oldRules = msg.oldRules;
        this.newRule = msg.newRule;
        this.newRules = msg.newRules;
        this.fieldIndex = msg.fieldIndex;
        this.fieldValues = msg.fieldValues;
    }


}
