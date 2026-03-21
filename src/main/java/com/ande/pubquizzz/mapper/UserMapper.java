package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", source = "appUserId")
    UserDTO toDTO(AppUser user);
}
