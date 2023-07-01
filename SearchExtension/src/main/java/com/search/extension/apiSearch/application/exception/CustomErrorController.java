package com.search.extension.apiSearch.application.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CustomErrorController extends BasicErrorController {

    @Autowired
    public CustomErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    @Override
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        Map<String, Object> model = new HashMap<>();
        model.put("status", status.value());
        model.put("errorMessage", getErrorAttributes(request, ErrorAttributeOptions.defaults()));

        // Custom logic for handling errors and setting views based on status codes
        String viewName;
        switch (status) {
            case NOT_FOUND:
                viewName = "error/404";
                break;
            case INTERNAL_SERVER_ERROR:
                viewName = "error/500";
                break;
            // Handle more specific status codes if needed
            default:
                viewName = "error/generic";
                break;
        }

        response.setStatus(status.value());
        return new ModelAndView(viewName, model);
    }

    @Override
    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        body.put("status", status.value());

        return new ResponseEntity<>(body, status);
    }

//    @Override
//    public String getErrorPath() {
//        return null; // Use the default error path
//    }
}
