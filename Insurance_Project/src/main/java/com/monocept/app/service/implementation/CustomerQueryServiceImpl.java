package com.monocept.app.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monocept.app.dto.CustomerQueryReplyDto;
import com.monocept.app.dto.CustomerQueryRequestDto;
import com.monocept.app.dto.CustomerQueryResponseDto;
import com.monocept.app.enums.QueryStatus;
import com.monocept.app.exception.CustomExceptions;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.CustomerQuery;
import com.monocept.app.model.User;
import com.monocept.app.repository.CustomerQueryRepository;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.service.CustomerQueryService;
import com.monocept.app.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerQueryServiceImpl implements CustomerQueryService {

    private final CustomerQueryRepository queryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public CustomerQueryResponseDto createQuery(CustomerQueryRequestDto dto) {
        log.info("Submitting new customer query for email: {}", dto.getEmail());

        CustomerQuery query = CustomerQuery.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .status(QueryStatus.PENDING)
                .build();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                query.setUser(user);
                query.setFullName(user.getFullName());
                query.setEmail(user.getEmail());
            });
        }

        CustomerQuery savedQuery = queryRepository.save(query);

        // Optional email confirmation to user
        try {
            emailService.sendEmail(
                savedQuery.getEmail(),
                "Query Received - " + savedQuery.getSubject(),
                "<p>Dear " + savedQuery.getFullName() + ",</p>" +
                "<p>Thank you for contacting Crown Assurance. We have received your inquiry: <b>\"" + savedQuery.getSubject() + "\"</b>.</p>" +
                "<p>Our support team will get back to you shortly.</p>" +
                "<p>Best regards,<br/>Crown Assurance Support Team</p>"
            );
        } catch (Exception ex) {
            log.error("Failed to send inquiry confirmation email", ex);
        }

        return convertToDto(savedQuery);
    }

    @Override
    public Page<CustomerQueryResponseDto> getMyQueries(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "";

        User loggedInUser = userRepository.findByEmail(email).orElse(null);
        if (loggedInUser != null) {
            return queryRepository.findByUser(loggedInUser, pageable).map(this::convertToDto);
        } else {
            return queryRepository.findByEmail(email, pageable).map(this::convertToDto);
        }
    }

    @Override
    public Page<CustomerQueryResponseDto> getAllQueries(Pageable pageable) {
        return queryRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional
    public CustomerQueryResponseDto replyToQuery(Long queryId, CustomerQueryReplyDto dto) {
        log.info("Replying to customer query ID: {}", queryId);

        // Verify support officer (Agent / Super Agent) authorization
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSupportAgent = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT")
                            || a.getAuthority().equals("ROLE_SUPER_AGENT"));

        if (!isSupportAgent) {
            throw new CustomExceptions.UnauthorizedAccessException("Admins can view queries, but only assigned support officers (Agents) are allowed to take action and reply to customer queries.");
        }

        CustomerQuery query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer Query not found with ID: " + queryId));

        if (query.getStatus() == QueryStatus.RESOLVED || query.getStatus() == QueryStatus.CLOSED) {
            throw new CustomExceptions.QueryAlreadyResolvedException("This customer query has already been resolved or closed and its status cannot be changed further.");
        }

        query.setResponse(dto.getResponse());
        query.setStatus(dto.getStatus() != null ? dto.getStatus() : QueryStatus.RESOLVED);

        CustomerQuery updatedQuery = queryRepository.save(query);

        // Send email response back to customer
        try {
            emailService.sendEmail(
                updatedQuery.getEmail(),
                "Response to your inquiry: " + updatedQuery.getSubject(),
                "<p>Dear " + updatedQuery.getFullName() + ",</p>" +
                "<p>Regarding your inquiry <b>\"" + updatedQuery.getSubject() + "\"</b>:</p>" +
                "<blockquote style=\"border-left: 3px solid #2563eb; padding-left: 10px; color: #475569;\">" + dto.getResponse() + "</blockquote>" +
                "<p>Status: <b>" + updatedQuery.getStatus() + "</b></p>" +
                "<p>If you have further questions, feel free to reply to this email.</p>" +
                "<p>Best regards,<br/>Crown Assurance Support Team</p>"
            );
        } catch (Exception ex) {
            log.error("Failed to send query reply email", ex);
        }

        return convertToDto(updatedQuery);
    }

    private CustomerQueryResponseDto convertToDto(CustomerQuery query) {
        CustomerQueryResponseDto dto = modelMapper.map(query, CustomerQueryResponseDto.class);
        if (query.getUser() != null) {
            dto.setUserId(query.getUser().getId());
        }
        return dto;
    }
}
