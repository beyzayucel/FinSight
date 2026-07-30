package com.akademi.finsight.auth.verificationtoken.mapper;

import com.akademi.finsight.auth.verificationtoken.entity.VerificationToken;
import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface VerificationTokenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    VerificationToken toEntity(User user, String token);
}
