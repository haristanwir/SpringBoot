package com.esb;

import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		String name = authentication.getName();
		String password = authentication.getCredentials().toString();

		if (name.equals("haris") && password.equals("tanwir")) {
			return new UsernamePasswordAuthenticationToken(name, password, new ArrayList<>());
		} else {
			throw new BadCredentialsException("Authentication failed");
		}
	}

	@Override
	public boolean supports(Class<?> auth) {
		return auth.equals(UsernamePasswordAuthenticationToken.class);
	}
}