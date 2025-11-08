package com.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.order.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
		return new ErrorResponse("NOT_FOUND", ex.getMessage());
	}
	
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleNullPointerException(NullPointerException ex) {
		return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred: " + ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleGeneralException(Exception ex) {
		return new ErrorResponse("INTERNAL_SERVER_ERROR", ex.getMessage());
	}

}
