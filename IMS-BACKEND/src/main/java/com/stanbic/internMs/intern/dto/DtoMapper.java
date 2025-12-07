package com.stanbic.internMs.intern.dto;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
@Slf4j
public class DtoMapper {
    public static <T> T mapToEntity(GenericDTO dto, Class<T> entityClass){
        try{
            T entity=entityClass.getDeclaredConstructor().newInstance();

            for(Field field: entityClass.getDeclaredFields()){
                field.setAccessible(true);
                Object value=dto.get(field.getName());
                log.info("Attempting to set "+ field + " as "+ value);
                if (value !=null){
//                    Handle type conversion(e.g., String -> long)
                    Object convertedValue=convertValue(value, field.getType());
                    field.set(entity, convertedValue);
                }
                log.info("set " + field + " Successfully" );
            }
            return entity;
        } catch (Exception e){
            throw new RuntimeException("Mapping failed for " +entityClass.getSimpleName(), e);
        }
    }

    public static <T> void mapToExistingEntity(GenericDTO dto, T entity){
        try{
            Class<?> entityClass=entity.getClass();

            for(Field field : entityClass.getDeclaredFields()){
                field.setAccessible(true);
                Object value=dto.get(field.getName());

                if (value !=null){
                    Object converted=convertValue(value, field.getType());
                    field.set(entity,converted);
                }
            }
        } catch (Exception e){
            throw new RuntimeException("Mapping to existing entity failed", e);
        }
    }

    public static <T> void mapNonNullFields(GenericDTO dto, T entity){
        dto.getFields().forEach((key, value)->{
            if(value !=null){
                try{
                   Field field=entity.getClass().getDeclaredField(key);
                   field.setAccessible(true);
                   field.set(entity, value);
                } catch (Exception e){
                    throw new RuntimeException("Error mapping field: " + key);
                }
            }
        });
    }

    private static Object convertValue(Object value, Class<?> targetType){
        if(value==null) return null;

        if(targetType.isAssignableFrom(value.getClass())){
            return value;
        }

        if(targetType.isEnum()){
            return Enum.valueOf((Class<Enum>) targetType, value.toString().toUpperCase());
        }

        if(targetType== Long.class || targetType==long.class){
            return Long.parseLong(value.toString());
        }

        if (targetType==Integer.class || targetType==int.class){
            return Integer.parseInt(value.toString());
        }

        if(targetType==Double.class || targetType==double.class){
            return Double.parseDouble(value.toString());
        }

        if (targetType==Boolean.class || targetType==boolean.class){
            return Boolean.parseBoolean(value.toString());
        }

        if(targetType==String.class){
            return value.toString();
        }
        return value;
    }
}
