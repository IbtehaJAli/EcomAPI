package com.ibtehaj.Ecom.Response;

import java.util.List;

public class ExceptionResponse {
	private List<String> exceptions;

    public ExceptionResponse(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    public List<String> getEexceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }
}
