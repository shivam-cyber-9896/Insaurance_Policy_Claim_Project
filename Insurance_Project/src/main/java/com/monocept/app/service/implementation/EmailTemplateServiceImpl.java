package com.monocept.app.service.implementation;

import org.springframework.stereotype.Service;

import com.monocept.app.service.EmailTempleteService;

@Service
public class EmailTemplateServiceImpl implements EmailTempleteService {

	// ── Auth ──────────────────────────────────────────────
	@Override
	public String welcomeTemplate(String fullName, String email) {
		return """
				<h2>Welcome, %s!</h2>
				<p>Your account has been successfully created.</p>
				<table>
				    <tr><td><b>Email:</b></td><td>%s</td></tr>
				</table>
				<p>You can now log in and explore our insurance plans.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, email);
	}

	@Override
	public String loginAlertTemplate(String fullName, String email, String loginTime) {
		return """
				<h2>Dear %s,</h2>
				<p>A new login was detected on your account.</p>
				<table>
				    <tr><td><b>Email:</b></td><td>%s</td></tr>
				    <tr><td><b>Time:</b></td><td>%s</td></tr>
				</table>
				<p>If this was not you, please contact support immediately.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, email, loginTime);
	}

	@Override
	public String passwordChangedTemplate(String fullName) {
		return """
				<h2>Dear %s,</h2>
				<p>Your password has been successfully changed.</p>
				<p>If you did not make this change, please contact support immediately.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName);
	}

	@Override
	public String forgotPasswordTemplate(String fullName, String resetToken) {
		return """
				<h2>Dear %s,</h2>
				<p>We received a request to reset your password.</p>
				<p>Use the token below to reset your password. It expires in 15 minutes.</p>
				<h3 style="color:#2d89ef;">%s</h3>
				<p>If you did not request this, ignore this email.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, resetToken);
	}

	// ── Customer ──────────────────────────────────────────
	@Override
	public String customerProfileCreatedTemplate(String fullName) {
		return """
				<h2>Dear %s,</h2>
				<p>Your customer profile has been successfully created.</p>
				<p>You can now browse and purchase insurance plans.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName);
	}

	@Override
	public String customerProfileUpdatedTemplate(String fullName) {
		return """
				<h2>Dear %s,</h2>
				<p>Your customer profile has been successfully updated.</p>
				<p>If you did not make this change, please contact support immediately.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName);
	}

	// ── Policy ────────────────────────────────────────────
	@Override
	public String policyCreatedTemplate(String fullName, String policyNumber, String planName, String startDate,
			String endDate, String premiumAmount) {
		return """
				<h2>Dear %s,</h2>
				<p>Your policy has been successfully created.</p>
				<table>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Plan Name:</b></td><td>%s</td></tr>
				    <tr><td><b>Start Date:</b></td><td>%s</td></tr>
				    <tr><td><b>End Date:</b></td><td>%s</td></tr>
				    <tr><td><b>Premium Amount:</b></td><td>%s</td></tr>
				    <tr><td><b>Status:</b></td><td>ACTIVE</td></tr>
				</table>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, policyNumber, planName, startDate, endDate, premiumAmount);
	}

	@Override
	public String policyCancelledTemplate(String fullName, String policyNumber) {
		return """
				<h2>Dear %s,</h2>
				<p>Your policy has been cancelled.</p>
				<table>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Status:</b></td><td>CANCELLED</td></tr>
				</table>
				<p>If you did not request this, please contact support immediately.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, policyNumber);
	}

	@Override
	public String policyExpiryReminderTemplate(String fullName, String policyNumber, String expiryDate) {
		return """
				<h2>Dear %s,</h2>
				<p>Your policy is expiring soon. Please renew it to continue coverage.</p>
				<table>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Expiry Date:</b></td><td>%s</td></tr>
				</table>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, policyNumber, expiryDate);
	}

	// ── Premium Payment ───────────────────────────────────
	@Override
	public String paymentSuccessTemplate(String fullName, String policyNumber, String amount, String transactionRef,
			String paymentDate) {
		return """
				<h2>Dear %s,</h2>
				<p>Your premium payment has been successfully processed.</p>
				<table>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Amount Paid:</b></td><td>₹%s</td></tr>
				    <tr><td><b>Transaction Ref:</b></td><td>%s</td></tr>
				    <tr><td><b>Payment Date:</b></td><td>%s</td></tr>
				    <tr><td><b>Status:</b></td><td>SUCCESS</td></tr>
				</table>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, policyNumber, amount, transactionRef, paymentDate);
	}

	@Override
	public String paymentFailedTemplate(String fullName, String policyNumber, String amount) {
		return """
				<h2>Dear %s,</h2>
				<p>Your premium payment has failed.</p>
				<table>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Amount:</b></td><td>₹%s</td></tr>
				    <tr><td><b>Status:</b></td><td>FAILED</td></tr>
				</table>
				<p>Please try again or contact support.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, policyNumber, amount);
	}

	// ── Claim ─────────────────────────────────────────────
	@Override
	public String claimSubmittedTemplate(String fullName, String claimNumber, String policyNumber) {
		return """
				<h2>Dear %s,</h2>
				<p>Your claim has been successfully submitted.</p>
				<table>
				    <tr><td><b>Claim Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				    <tr><td><b>Status:</b></td><td>SUBMITTED</td></tr>
				</table>
				<p>We will review your claim and get back to you shortly.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, claimNumber, policyNumber);
	}

	@Override
	public String claimStatusUpdatedTemplate(String fullName, String claimNumber, String status, String remarks) {
		return """
				<h2>Dear %s,</h2>
				<p>Your claim status has been updated.</p>
				<table>
				    <tr><td><b>Claim Number:</b></td><td>%s</td></tr>
				    <tr><td><b>New Status:</b></td><td>%s</td></tr>
				    <tr><td><b>Remarks:</b></td><td>%s</td></tr>
				</table>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, claimNumber, status, remarks);
	}

	// ── Agent ─────────────────────────────────────────────
	@Override
	public String agentRegisteredTemplate(String fullName, String email, String tempPassword) {
		return """
				<h2>Dear %s,</h2>
				<p>Your agent account has been created.</p>
				<table>
				    <tr><td><b>Email:</b></td><td>%s</td></tr>
				    <tr><td><b>Temporary Password:</b></td><td>%s</td></tr>
				</table>
				<p>Please log in and change your password immediately.</p>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, email, tempPassword);
	}

	@Override
	public String agentAssignedTemplate(String fullName, String customerName, String policyNumber) {
		return """
				<h2>Dear %s,</h2>
				<p>You have been assigned to a new policy.</p>
				<table>
				    <tr><td><b>Customer Name:</b></td><td>%s</td></tr>
				    <tr><td><b>Policy Number:</b></td><td>%s</td></tr>
				</table>
				<br><p>Regards,<br>Insurance Team</p>
				""".formatted(fullName, customerName, policyNumber);
	}
	public String accountStatusTemplate(String fullName, String status) {
	    return """
	        <h2>Dear %s,</h2>
	        <p>Your account has been <b>%s</b> by the administrator.</p>
	        <p>If you have any questions, please contact support.</p>
	        <br><p>Regards,<br>Insurance Team</p>
	        """.formatted(fullName, status);
	}

}