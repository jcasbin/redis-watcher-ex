// Copyright 2023 The casbin Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.casbin.watcherEx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.casbin.jcasbin.persist.WatcherEx;

import java.io.Serializable;
import java.util.List;


public class Msg implements Serializable {
    private WatcherEx.UpdateType method;
    private String id;
    private String sec;
    private String ptype;
    private List<String> oldRule;
    private List<List<String>> oldRules;
    private List<String> newRule;
    private List<List<String>> newRules;
    private int fieldIndex;
    private List<String> fieldValues;

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

    public List<String> getOldRule() {
        return oldRule;
    }

    public void setOldRule(List<String> oldRule) {
        this.oldRule = oldRule;
    }

    public List<List<String>> getOldRules() {
        return oldRules;
    }

    public void setOldRules(List<List<String>> oldRules) {
        this.oldRules = oldRules;
    }

    public List<String> getNewRule() {
        return newRule;
    }

    public void setNewRule(List<String> newRule) {
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

    public List<String> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<String> fieldValues) {
        this.fieldValues = fieldValues;
    }


    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Msg fromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, Msg.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
