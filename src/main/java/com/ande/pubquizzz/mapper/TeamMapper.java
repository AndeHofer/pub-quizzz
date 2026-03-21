package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.dto.TeamDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    TeamDTO toDTO(Team team);
}
