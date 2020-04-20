package io.ppmtool.services;

import io.ppmtool.domain.Project;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapValidationErrorService {

    public ResponseEntity<?> MapValidationService(BindingResult result){
        if(result.hasErrors()){
            Map<String, String> errMap = new HashMap<>();
            for(FieldError err: result.getFieldErrors()){
                errMap.put(err.getField(), err.getDefaultMessage());
            }
            return new ResponseEntity<Map<String, String>>(errMap, HttpStatus.BAD_REQUEST);
        }
        return null;
    }

}
