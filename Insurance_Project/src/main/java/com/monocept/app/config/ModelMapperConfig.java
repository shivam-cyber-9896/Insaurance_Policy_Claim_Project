package com.monocept.app.config;

import com.monocept.app.model.User;
import com.monocept.app.dto.LoginResponseDto;
import com.monocept.app.dto.RegisterRequestDto;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }
}
