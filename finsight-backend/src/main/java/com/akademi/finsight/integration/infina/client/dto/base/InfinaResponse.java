package com.akademi.finsight.integration.infina.client.dto.base;

public record InfinaResponse<T>(Result<T> result){

	public record Result<T>(T data, Summary summary){}

	public record Summary(int resultCode, String resultMessage, Integer rows){}
}
