package com.smartpayments.scheduler.application.config.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.smartpayments.scheduler.core.common.exception.UserNotFoundException;
import com.smartpayments.scheduler.core.ports.out.external.authenticator.AuthenticatorClientPort;
import com.smartpayments.scheduler.core.ports.out.external.dto.UserAuthenticatorOutput;
import com.smartpayments.scheduler.core.ports.out.utils.JwtUtilsPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class SecurityFilterConfig extends OncePerRequestFilter {
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

            if (jwtDecoded.getSubject().isEmpty()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Subject missing.");
                return;
            }

            UserAuthenticatorOutput authenticatorOutput = authenticatorClientPort.findUser(Long.valueOf(jwtDecoded.getSubject()));

            if (!authenticatorOutput.getActive()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "User not active.");
                return;
            }

            UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(authenticatorOutput.getId())
                .userRoles(authenticatorOutput.getRoles())
                .hashPassword(authenticatorOutput.getPasswordHash())
                .isActive(authenticatorOutput.getActive())
                .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatorOutput.getId(),
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

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (isNull(token) || !token.startsWith("Bearer")) return null;

        return token.replace("Bearer ", "");
    }
}
