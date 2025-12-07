package com.stanbic.internMs.intern.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Map;
import java.util.HashMap;

public class GenericDTO {
    private final Map<String, Object> fields= new HashMap<>();

    @JsonAnySetter
    public void set(String key, Object value){fields.put(key, value);}

    @JsonAnyGetter
    public Map<String, Object> getFields(){return fields;}

    public Object get(String key){return fields.get(key);}

    public String getString(String key){
        Object val=fields.get(key);
        return val !=null ? val.toString() : null;
    }


}
