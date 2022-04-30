package tr.com.app.registration.services;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.com.app.registration.dto.RegistrationRequest;
import tr.com.app.registration.entity.AppUser;
import tr.com.app.registration.enums.AppUserRole;
import tr.com.app.registration.token.ConfirmationToken;
import tr.com.app.registration.token.ConfirmationTokenService;
import tr.com.app.registration.valid.EmailValidation;

@Service
public class RegistrationService {

	@Autowired
	private EmailValidation emailValidation;

	@Autowired
	private AppUserService appUserService;

	@Autowired
	private ConfirmationTokenService confirmationTokenService;

	public String register(RegistrationRequest request) {
		boolean isValidEmail = emailValidation.test(request.getEmail());
		if (!isValidEmail) {
			throw new IllegalStateException("email not valid");
		}
		return appUserService.signUpUser(new AppUser(request.getFirstName(), request.getLastName(), request.getEmail(),
				request.getPassword(), AppUserRole.USER));

	}

	@Transactional
	public String confirmToken(String token) {
		ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
				.orElseThrow(() -> new IllegalStateException("token not found"));
		
		 if (confirmationToken.getConfirmedAt() != null) {
	            throw new IllegalStateException("email already confirmed");
	        }

		LocalDateTime expiredAt = confirmationToken.getExpiresAt();

		if (expiredAt.isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("token expired");
		}
		
		 confirmationTokenService.setConfirmedAt(token);
	        appUserService.enableAppUser(
	                confirmationToken.getAppUser().getEmail());
	        
	        return "confirmed";

	}
}
