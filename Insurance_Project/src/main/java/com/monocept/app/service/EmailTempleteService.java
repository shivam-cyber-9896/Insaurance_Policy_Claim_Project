package com.monocept.app.service;

public interface EmailTempleteService {

	// ── Auth ──────────────────────────────────────────────
	String welcomeTemplate(String fullName, String email);

	String loginAlertTemplate(String fullName, String email, String loginTime);

	String passwordChangedTemplate(String fullName);

	String forgotPasswordTemplate(String fullName, String resetToken);

	// ── Customer ──────────────────────────────────────────
	String customerProfileCreatedTemplate(String fullName);

	String customerProfileUpdatedTemplate(String fullName);

	// ── Policy ────────────────────────────────────────────
	String policyCreatedTemplate(String fullName, String policyNumber, String planName, String startDate,
			String endDate, String premiumAmount);

	String policyCancelledTemplate(String fullName, String policyNumber);

	String policyExpiryReminderTemplate(String fullName, String policyNumber, String expiryDate);

	// ── Premium Payment ───────────────────────────────────
	String paymentSuccessTemplate(String fullName, String policyNumber, String amount, String transactionRef,
			String paymentDate);

	String paymentFailedTemplate(String fullName, String policyNumber, String amount);

	// ── Claim ─────────────────────────────────────────────
	String claimSubmittedTemplate(String fullName, String claimNumber, String policyNumber);

	String claimStatusUpdatedTemplate(String fullName, String claimNumber, String status, String remarks);

	// ── Agent ─────────────────────────────────────────────
	String agentRegisteredTemplate(String fullName, String email, String tempPassword);

	String agentAssignedTemplate(String fullName, String customerName, String policyNumber);
	 String accountStatusTemplate(String fullName, String status);
}