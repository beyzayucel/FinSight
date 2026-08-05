package com.akademi.finsight.integration.infina.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InfinaErrorType implements BaseErrorType {

	INFINA_UNAVAILABLE("error.infina.services.unavailable", HttpStatus.SERVICE_UNAVAILABLE),
	INFINA_ERROR_RESPONSE("error.infina.services.error.response", HttpStatus.BAD_GATEWAY);

	private final String messageKey;
	private final HttpStatus httpStatus;

	@Override
	public String getCode(){
		return name();
	}

}
