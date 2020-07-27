package pucp.middlewareiot.usuarios.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pucp.middlewareiot.usuarios.objects.UserLogin;
import pucp.middlewareiot.usuarios.services.UserService;
import reactor.core.publisher.Mono;
import sh.ory.hydra.ApiClient;
import sh.ory.hydra.ApiException;
import sh.ory.hydra.Configuration;
import sh.ory.hydra.api.AdminApi;
import sh.ory.hydra.model.*;

@Slf4j
@Controller
public class LoginController {
    private final AdminApi apiInstance;
    private final UserService userService;
    
    @Value("${hydra.url}")
    private String hydraUrl;
    
    @Autowired
    public LoginController(UserService userService) {
        // Initializing Hydra SDK
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setVerifyingSsl(false);
        apiClient.setBasePath(hydraUrl);
        this.apiInstance = new AdminApi(apiClient);
        
        this.userService = userService;
    }
    
    @GetMapping(value = "/login")
    public String loginFlow(@RequestParam String login_challenge, Model model) {
        try {
            LoginRequest loginRequest = apiInstance.getLoginRequest(login_challenge);
            if(loginRequest.getSkip()) {
                AcceptLoginRequest acceptRequest = new AcceptLoginRequest();
                acceptRequest = acceptRequest.subject(loginRequest.getSubject());
                acceptRequest = acceptRequest.remember(true);
                acceptRequest = acceptRequest.rememberFor(86400L);
                CompletedRequest request = apiInstance.acceptLoginRequest(login_challenge, acceptRequest);
                return "redirect:" + request.getRedirectTo();
            }
            model.addAttribute("login_challenge", login_challenge);
            return "login";
        }
        catch(ApiException e) {
            log.error(e.getResponseBody());
            return "error";
        }
    }
    
    @PostMapping(value = "/loginForm")
    public Mono<String> loginProcess(UserLogin userLogin) {
        Mono<Boolean> passwordChecked = userService.checkPassword(userLogin.getUsername(), userLogin.getPassword());
        return passwordChecked
                .flatMap(aBoolean -> {
                    if(aBoolean) {
                        AcceptLoginRequest acceptRequest = new AcceptLoginRequest();
                        acceptRequest = acceptRequest.subject(userLogin.getUsername());
                        acceptRequest = acceptRequest.remember(true);
                        acceptRequest = acceptRequest.rememberFor(86400L);
                        try {
                            CompletedRequest request = apiInstance.acceptLoginRequest(userLogin.getLogin_challenge(),
                                    acceptRequest);
                            return Mono.just("redirect:" + request.getRedirectTo());
                        } catch(ApiException e) {
                            log.error(e.getResponseBody());
                            return Mono.just("error");
                        }
                    }
                    return Mono.just("redirect:/loginForm?error=1&login_challenge=" + userLogin.getLogin_challenge());
                })
                .switchIfEmpty(Mono.just("redirect:/loginForm?error=1&login_challenge="
                        + userLogin.getLogin_challenge()));
    }
    
    @GetMapping(value = "/loginForm")
    public String loginForm(@RequestParam Integer error,
                            @RequestParam String login_challenge,
                            Model model) {
        if(error == 1) model.addAttribute("error", "Credenciales incorrectas");
        model.addAttribute("login_challenge", login_challenge);
        return "login";
    }
    
    @GetMapping(value = "/consent")
    public String consentFlow(@RequestParam String consent_challenge) {
        try {
            ConsentRequest consentRequest = apiInstance.getConsentRequest(consent_challenge);
            
            // We accept all the consents required automatically, but can add a form to let users accept it
            AcceptConsentRequest acceptRequest = new AcceptConsentRequest();
            acceptRequest = acceptRequest.grantScope(consentRequest.getRequestedScope());
            acceptRequest = acceptRequest.grantAccessTokenAudience(consentRequest.getRequestedAccessTokenAudience());
            acceptRequest = acceptRequest.remember(true);
            acceptRequest = acceptRequest.rememberFor(86400L);
            CompletedRequest request = apiInstance.acceptConsentRequest(consent_challenge, acceptRequest);
            return "redirect:" + request.getRedirectTo();
        }
        catch(ApiException e) {
            log.error(e.getResponseBody());
            return "error";
        }
    }
}
