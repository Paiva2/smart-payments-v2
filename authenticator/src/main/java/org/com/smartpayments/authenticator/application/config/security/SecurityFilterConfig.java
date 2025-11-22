package org.com.smartpayments.authenticator.application.config.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.AuthenticationBlackListDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.JwtUtilsPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class SecurityFilterConfig extends OncePerRequestFilter {
    public final List<AntPathRequestMatcher> NON_FILTERABLE_ENDPOINTS = List.of(
        new AntPathRequestMatcher("/api/authenticator/user/register"),
        new AntPathRequestMatcher("/api/authenticator/user/auth"),
        new AntPathRequestMatcher("/api/authenticator/user/email_activation/{token}"),
        new AntPathRequestMatcher("/api/authenticator/user/email_activation"),
        new AntPathRequestMatcher("/api/authenticator/user/internal")
    );

    private final UserDataProviderPort userDataProviderPort;
    private final AuthenticationBlackListDataProviderPort authenticationBlackListDataProviderPort;
    private final JwtUtilsPort jwtUtilsPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);

        if (isNull(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token missing.");
            return;
        }

        if (authenticationBlackListDataProviderPort.existsByTokenHash(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token invalid.");
            return;
        }

        try {
            DecodedJWT jwtDecoded = jwtUtilsPort.verifyAuthJwt(token);
            User user = userDataProviderPort.findByIdWithRoles(Long.valueOf(jwtDecoded.getSubject()))
                .orElseThrow(UserNotFoundException::new);

            UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(user.getId())
                .userRoles(user.getUserRoles())
                .hashPassword(user.getPasswordHash())
                .isActive(nonNull(user.getEmailConfirmedAt()) && user.getActive())
                .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JWTVerificationException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token.");
        } catch (UserNotFoundException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "User not found.");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return NON_FILTERABLE_ENDPOINTS.stream().anyMatch(matcher -> matcher.matches(request));
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (isNull(token) || !token.startsWith("Bearer")) return null;

        return token.replace("Bearer ", "");
    }
}
