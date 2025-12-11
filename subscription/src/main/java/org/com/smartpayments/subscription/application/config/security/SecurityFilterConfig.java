package org.com.smartpayments.subscription.application.config.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.com.smartpayments.subscription.core.common.exception.UserNotFoundException;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.UserDataProviderPort;
import org.com.smartpayments.subscription.core.ports.out.external.authenticator.AuthenticatorClientPort;
import org.com.smartpayments.subscription.core.ports.out.external.dto.UserAuthenticatorOutput;
import org.com.smartpayments.subscription.core.ports.out.utils.JwtUtilsPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class SecurityFilterConfig extends OncePerRequestFilter {
    public final List<AntPathRequestMatcher> NON_FILTERABLE_ENDPOINTS = List.of(
        new AntPathRequestMatcher("/api/subscription/purchase/webhook")
    );
    public final List<AntPathRequestMatcher> NON_FILTERABLE_ENDPOINTS_INTERNAL = List.of();

    private final UserDataProviderPort userDataProviderPort;

    private final AuthenticatorClientPort authenticatorClientPort;

    private final JwtUtilsPort jwtUtilsPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);

        if (isNull(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token missing.");
            return;
        }

        try {
            DecodedJWT jwtDecoded = jwtUtilsPort.verifyAuthJwt(token);
            Optional<User> user = userDataProviderPort.findActiveById(Long.valueOf(jwtDecoded.getSubject()));

            if (user.isEmpty()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token missing.");
                return;
            }

            UserAuthenticatorOutput authenticatorOutput = authenticatorClientPort.findUser(user.get().getId());

            if (!authenticatorOutput.getActive()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "User not active.");
                return;
            }

            UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(user.get().getId())
                .userRoles(authenticatorOutput.getRoles())
                .hashPassword(authenticatorOutput.getPasswordHash())
                .isActive(authenticatorOutput.getActive())
                .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.get().getId(),
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
        return NON_FILTERABLE_ENDPOINTS.stream().anyMatch(matcher -> matcher.matches(request)) ||
            NON_FILTERABLE_ENDPOINTS_INTERNAL.stream().anyMatch(matcher -> matcher.matches(request));
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (isNull(token) || !token.startsWith("Bearer")) return null;

        return token.replace("Bearer ", "");
    }
}
