package com.monocept.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.monocept.app.dto.CustomerQueryReplyDto;
import com.monocept.app.dto.CustomerQueryRequestDto;
import com.monocept.app.dto.CustomerQueryResponseDto;

public interface CustomerQueryService {

    CustomerQueryResponseDto createQuery(CustomerQueryRequestDto dto);

    Page<CustomerQueryResponseDto> getMyQueries(Pageable pageable);

    Page<CustomerQueryResponseDto> getAllQueries(Pageable pageable);

    CustomerQueryResponseDto replyToQuery(Long queryId, CustomerQueryReplyDto dto);
}
