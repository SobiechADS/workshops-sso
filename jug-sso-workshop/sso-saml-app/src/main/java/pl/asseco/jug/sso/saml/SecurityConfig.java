package pl.asseco.jug.sso.saml;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.saml2.core.Attribute;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.saml2.provider.service.registration.*;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

//    @Bean
//    public RelyingPartyRegistrationRepository relyingPartyRegistrations() {
//        RelyingPartyRegistration rpr = RelyingPartyRegistrations
//
//                .fromMetadataLocation(config.getMetadataUri())
//                .assertingPartyMetadata(builder ->
//                        builder.wantAuthnRequestsSigned(config.getWantAuthnRequestsSigned())
//                        .singleLogoutServiceLocation(config.getLogoutUrl())
//                                .singleLogoutServiceBinding(config.getLogoutBinding()))
//                .authnRequestsSigned(config.getWantAuthnRequestsSigned())
//                .entityId(config.getEntityId())
//                .singleLogoutServiceLocation(config.getLogoutUrl())
//                .singleLogoutServiceBinding(config.getLogoutBinding())
//                .registrationId(config.getId()).build();
//        return new InMemoryRelyingPartyRegistrationRepository(rpr);
//    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
//                .saml2Login(Customizer.withDefaults())
//                .saml2Logout(Customizer.withDefaults());
//        return http.build();
//    }


///  Mapowanie ról
////
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .saml2Login(saml2 -> saml2.authenticationManager(provider()))
                .saml2Logout(Customizer.withDefaults());
        return http.build();
    }

    private ProviderManager provider() {
        OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
        provider.setResponseAuthenticationConverter(token -> {
            Saml2Authentication authentication = OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter().convert(token);
            List<SimpleGrantedAuthority> roleAttributes = getRoles(token);
            if (roleAttributes.isEmpty()) {
                throw new Saml2AuthenticationException(new Saml2Error("1234", "No roles found"));
            }
            return new Saml2Authentication((AuthenticatedPrincipal) authentication.getPrincipal(), authentication.getSaml2Response(), roleAttributes);
        });
        return new ProviderManager(provider);
    }

    private static List<SimpleGrantedAuthority> getRoles(OpenSaml4AuthenticationProvider.ResponseToken token) {
        return token.getResponse().getAssertions().stream()
                .flatMap(as -> as.getAttributeStatements().stream())
                .flatMap(attrs -> attrs.getAttributes().stream())
                .filter(attrs -> attrs.getName().equals("Role"))
                .map(Attribute::getAttributeValues)
                .flatMap(objects ->
                    objects.stream()
                            .map(XSStringImpl.class::cast)
                            .map(XSStringImpl::getValue)
                            .filter(StringUtils::isNotBlank)
                            .map(foundValue -> new SimpleGrantedAuthority("ROLE_" + foundValue.toUpperCase().replaceFirst("/", "")))
                ).toList();
    }


}
