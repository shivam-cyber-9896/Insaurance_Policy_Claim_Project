package com.monocept.app.dto;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDto {
	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be in the past")
	private LocalDate dateOfBirth;

    @AssertTrue(message = "User must be at least 18 years old")
    public boolean isAdult() {

        if (dateOfBirth == null) {
            return true;
            // @NotNull will handle null separately
        }
        return !dateOfBirth.plusYears(18).isAfter(LocalDate.now());
    }
	@NotBlank(message = "Address is required")
	private String address;

	@NotBlank(message = "City is required")
	private String city;

	@NotBlank(message = "State is required")
	private String state;

	@NotBlank(message = "6 Digit PIN code is required")
	@Pattern(regexp = "^[1-9][0-9]{5}$", message = "PIN code must be exactly 6 digits starting with non-zero")
	private String pinCode;

	@NotBlank(message = "Nominee name is required")
	private String nomineeName;

	@NotBlank(message = "Nominee relation is required")
	private String nomineeRelation;
}
