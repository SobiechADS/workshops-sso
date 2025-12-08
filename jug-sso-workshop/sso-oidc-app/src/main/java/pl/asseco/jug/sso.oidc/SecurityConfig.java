package pl.asseco.jug.sso.oidc;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository, JwtAuthenticationConverter jwtAuthConverter, RolesAuthoritiesMapper rolesAuthoritiesMapper) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2Login(oauth -> oauth.userInfoEndpoint(u -> u.userAuthoritiesMapper(rolesAuthoritiesMapper)))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .logout(customizer -> customizer.logoutSuccessHandler(logoutSuccessHandler(clientRegistrationRepository)));
        return http.build();
    }

    public OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler oidcLogout = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogout.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogout;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthConverter(RolesAuthoritiesMapper rolesAuthoritiesMapper) {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(rolesAuthoritiesMapper);
        return converter;
    }

}