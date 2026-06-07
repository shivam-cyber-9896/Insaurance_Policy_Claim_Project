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
        
        // Skip ID field mapping to prevent matching DTO reference IDs (like productId, policyId) to the entity primary key id.
        modelMapper.typeMap(com.monocept.app.dto.PlanRequestDto.class, com.monocept.app.model.PolicyPlan.class)
            .addMappings(mapper -> mapper.skip(com.monocept.app.model.PolicyPlan::setId));
            
        modelMapper.typeMap(com.monocept.app.dto.PaymentRequestDto.class, com.monocept.app.model.PremiumPayment.class)
            .addMappings(mapper -> mapper.skip(com.monocept.app.model.PremiumPayment::setId));
            
        modelMapper.typeMap(com.monocept.app.dto.ClaimRequestDto.class, com.monocept.app.model.Claim.class)
            .addMappings(mapper -> mapper.skip(com.monocept.app.model.Claim::setId));
            
        modelMapper.typeMap(com.monocept.app.dto.UserRequestDto.class, com.monocept.app.model.User.class)
            .addMappings(mapper -> {
                mapper.map(com.monocept.app.dto.UserRequestDto::getEmail, com.monocept.app.model.User::setMail);
                mapper.map(com.monocept.app.dto.UserRequestDto::getMobileNumber, com.monocept.app.model.User::setPhoneNumber);
            });
            
        modelMapper.typeMap(com.monocept.app.model.ClaimStatusHistory.class, com.monocept.app.dto.ClaimHistoryResponseDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getUpdatedBy().getMail(), com.monocept.app.dto.ClaimHistoryResponseDto::setUpdatedBy));
            
        return modelMapper;
    }
}
