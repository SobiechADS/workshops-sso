package pl.asseco.jug.sso.oidc;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AppController {

    @GetMapping("/none")
    public Principal hello(Principal principal) {
        return principal;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public Principal helloAdmin(Principal principal) {
        return principal;
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public Principal helloUser(Principal principal) {
        return principal;
    }

}
