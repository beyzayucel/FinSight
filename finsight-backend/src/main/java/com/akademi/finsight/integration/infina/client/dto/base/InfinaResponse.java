package com.akademi.finsight.integration.infina.client.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InfinaResponse<T>(Result<T> result){

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result<T>(T data, Summary summary){}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Summary(int resultCode, String resultMessage, Integer rows){}
}
