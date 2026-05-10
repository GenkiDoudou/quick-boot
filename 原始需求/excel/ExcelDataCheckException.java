package com.epsoft.common.excel;


public class ExcelDataCheckException extends RuntimeException {


    private static final long serialVersionUID = 1L;


    /**
     * 错误提示
     */
    private String message;


    public ExcelDataCheckException(String message) {
        this.message = message;
    }


    @Override
    public String getMessage() {
        return message;
    }

}
