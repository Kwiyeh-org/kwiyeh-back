package com.kwiyeh.back.firebase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.kwiyeh.back.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter implements Filter {

    @Value("${firebase.database.url}")
    private String googleMobileClientId;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String token = getBearerTokenFromRequest((HttpServletRequest) request);

        if (token != null) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(decodedToken.getUid(), null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (FirebaseAuthException e) {
                try {
                    Claims claims = JwtUtil.parseToken(token);
                    String username = claims.getSubject();

                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (JwtException e1) {
                    try{
                        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                        .setAudience(java.util.Collections.singletonList(googleMobileClientId))
                        .build();
                        GoogleIdToken idToken = verifier.verify(token);
                        UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(idToken.getPayload().getEmail());
                        UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userRecord.getUid(), null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                    catch(GeneralSecurityException | FirebaseAuthException e2){
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String getBearerTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }
}