package com.akademi.finsight.user.mapper;


import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.user.dto.CreateUserRequest;
import com.akademi.finsight.user.dto.UserResponse;
import com.akademi.finsight.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface UserMapper {

    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "firstLogin", constant = "true")
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);
}
