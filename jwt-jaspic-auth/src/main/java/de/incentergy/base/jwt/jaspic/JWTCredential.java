package de.incentergy.base.jwt.jaspic;
import java.util.Set;
import javax.security.enterprise.credential.Credential;

public class JWTCredential implements Credential {

    private final String principal;
    private final Set<String> groups;

    public JWTCredential(String principal, Set<String> groups) {
        this.principal = principal;
        this.groups = groups;
    }

    public String getPrincipal() {
        return principal;
    }

    public Set<String> getGroups() {
        return groups;
    }

}