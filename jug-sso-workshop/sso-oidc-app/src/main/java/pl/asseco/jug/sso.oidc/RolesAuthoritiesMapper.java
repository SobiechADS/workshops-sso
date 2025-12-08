package pl.asseco.jug.sso.oidc;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Component
public class RolesAuthoritiesMapper implements GrantedAuthoritiesMapper, Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String REALM_ACCESS = "realm_access";
    public static final String ROLES = "roles";

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mapped = new HashSet<>(authorities);
        authorities.forEach(a -> {
            Map<String, Object> claims = switch (a) {
                case OidcUserAuthority oidc -> oidc.getIdToken().getClaims();
                case OAuth2UserAuthority oauth2 -> oauth2.getAttributes();
                default -> Map.of();
            };
            var realmAccess = claims.get(REALM_ACCESS);
            if (realmAccess instanceof Map<?, ?> map) {
                ((Collection<?>) map.get(ROLES))
                        .stream().map(this::createAuthority)
                        .forEach(mapped::add);
            }
        });
        return new HashSet<>(mapped);
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> roles = new HashSet<>();
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
        if (realmAccess != null) {
            ((Collection<?>) realmAccess.get(ROLES))
                    .stream().map(this::createAuthority)
                    .forEach(roles::add);
        }
        return roles;
    }

    private SimpleGrantedAuthority createAuthority(Object role) {
        return new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase());
    }

}