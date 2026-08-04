package com.akademi.finsight.integration.infina.exception;

import com.akademi.finsight.common.exception.BaseException;

public class InfinaIntegrationException extends BaseException {

	public InfinaIntegrationException(InfinaErrorType infinaErrorType){
		super(infinaErrorType);
	}

	public InfinaIntegrationException(InfinaErrorType infinaErrorType, Throwable cause){
		super(infinaErrorType, cause);
	}
}
