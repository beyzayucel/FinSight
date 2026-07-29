package com.akademi.finsight.auth.service;


import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);


}
