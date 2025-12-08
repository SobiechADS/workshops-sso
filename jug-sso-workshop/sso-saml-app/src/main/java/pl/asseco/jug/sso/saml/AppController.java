package pl.asseco.jug.sso.saml;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AppController {

    //Principal => org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

    @GetMapping("/none")
    public Principal hello(Principal principal) {
        return principal;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping(value = "/admin")
    public Principal helloAdmin(Principal principal) {
        return principal;
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping(value = "/user")
    public Principal helloUser(Principal principal) {
        return principal;
    }

}