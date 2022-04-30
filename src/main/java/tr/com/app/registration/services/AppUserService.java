package tr.com.app.registration.services;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import tr.com.app.registration.entity.AppUser;
import tr.com.app.registration.repository.AppUserRepository;
import tr.com.app.registration.token.ConfirmationToken;
import tr.com.app.registration.token.ConfirmationTokenService;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

	private final static String USER_NOT_FOUND_MSG = "User with email %s not found";
	private final AppUserRepository userRepo;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private final ConfirmationTokenService tokenService;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		return userRepo.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
	}

	public String signUpUser(AppUser appUser) {
		boolean userExist = userRepo.findByEmail(appUser.getEmail()).isPresent();

		if (userExist) {
			throw new IllegalStateException("email already taken");
		}
		String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
		appUser.setPassword(encodedPassword);
		appUser.setCreatedDate(new Date());
		userRepo.save(appUser);

		String token = UUID.randomUUID().toString();
		ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
				LocalDateTime.now().plusMinutes(15), appUser);
		tokenService.saveConfirmationToken(confirmationToken);

		return token;
	}
	
	
	public int enableAppUser(String email) {
		return userRepo.enableAppUser(email);
	}

}
