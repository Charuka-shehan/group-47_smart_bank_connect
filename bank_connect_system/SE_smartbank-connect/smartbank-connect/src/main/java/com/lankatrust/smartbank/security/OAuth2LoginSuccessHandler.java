package com.lankatrust.smartbank.security;

import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles the Google Sign-In / Sign-Up (OAuth 2.0) flow described in
 * Section 1.3 of the proposal. On first login via Google, a new CUSTOMER
 * account is auto-provisioned (no password set, googleLinked = true).
 * On subsequent logins the existing user record is reused.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth");
            return;
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        request.getSession().removeAttribute("OTP_VERIFIED");
        request.getSession().setAttribute("OTP_SENT", Boolean.TRUE);
        setDefaultTargetUrl("/login/otp");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
