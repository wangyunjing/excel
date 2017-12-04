package com.wyj.core.excel.exception;

/**
 * Created by wyj on 17-12-4.
 */
public class ExcelExportException extends RuntimeException {
	public ExcelExportException() {
	}

	public ExcelExportException(String message) {
		super(message);
	}

	public ExcelExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExcelExportException(Throwable cause) {
		super(cause);
	}

	public ExcelExportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
