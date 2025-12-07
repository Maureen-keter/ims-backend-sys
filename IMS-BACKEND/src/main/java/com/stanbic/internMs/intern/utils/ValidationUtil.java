package com.stanbic.internMs.intern.utils;

import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ValidationUtil {
    public static <T> void validateRequiredFields(GenericDTO dto, Class<T> entityClass){
        Map<String, String> errors = new HashMap<>();

        for(Field field : entityClass.getDeclaredFields()){
            boolean required=false;

            //Check JPA @Column(nullable=false
            Column column=field.getAnnotation(Column.class);
            if(column !=null && !column.nullable()){
                required=true;
            }
//            check @NotNull
            if(field.isAnnotationPresent(NotNull.class)){
                required=true;
            }

            if(field.isAnnotationPresent(NotBlank.class)){
                required=true;
            }

            if(field.isAnnotationPresent(NotEmpty.class)){
                required=true;
            }

            if(required){
                String fieldName= field.getName();
                Object value=dto.get(fieldName);

                if (value==null || value.toString().isBlank()) {
                    errors.put(fieldName, fieldName + "is required");
                }
            }
        }

        if (!errors.isEmpty()){
            throw new ValidationException(errors);
        }
    }

    public static <T> void validateProvidedFields(GenericDTO dto, Class<T> entityClass){
       Map<String, String> errors = new HashMap<>();
       for (Field field: entityClass.getDeclaredFields()){
           field.setAccessible(true);
//           only validate present in DTO
           if(dto.getFields().containsKey(field.getName())){
               Object value=dto.getFields().get(field.getName());
               if(field.isAnnotationPresent(NotNull.class) && value==null){
                   errors.put(field.getName(), field.getName()+ "must not be null");
               }
           }
       }
       if(!errors.isEmpty()){
           throw new ValidationException(errors);
       }
    }
}
