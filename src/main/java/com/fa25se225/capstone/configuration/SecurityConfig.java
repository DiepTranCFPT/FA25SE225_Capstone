package com.fa25se225.capstone.configuration;

import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.PermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS_POST = {"/users","/auth/**", "auth/token", "auth/introspect", "auth/logout","/payment/momo/**", "auth/refresh-token", "auth/reset-password/**", "test/**","/auth/verify-email","/auth/outbound/authentication"};
    private final String[] PUBLIC_ENDPOINTS_GET = {"auth/forgot-password", "test/**", "learning-materials/public","/payment/momo/**", "exam-templates/browse", "exam-templates/ratings/**", "users/teachers", "/questions-v2/import/template", "/flashcard-sets","/{fileName}/materials","/api/learning-material-ratings/**","/learning-materials/search"};
    private static final String[] SWAGGER_UI_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/edcare/api/v1/swagger-ui/"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder customJwtDecoder,
                                                   JwtAuthenticationConverter customJwtAuthenticationConverter) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.configurationSource(request -> {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowCredentials(true);
                        config.addAllowedOriginPattern("*");
                        config.addAllowedHeader("*");
                        config.addAllowedMethod("*");
                        config.setMaxAge(3600L);
                        return config;
                    }))
                    .authorizeHttpRequests(auth ->
                            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                    .requestMatchers(SWAGGER_UI_PATHS).permitAll()
                                    .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS_POST).permitAll()
                                    .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS_GET).permitAll()
                                    .requestMatchers("/subjects/**").permitAll()
                                    .anyRequest().authenticated());

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(customJwtAuthenticationConverter))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
            return http.build();


    }

//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter(){
//        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
//        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
//        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
//
//        return authenticationConverter;
//
//    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(PermissionService permissionService){
        JwtGrantedAuthoritiesConverter roleConverter = new JwtGrantedAuthoritiesConverter();
        roleConverter.setAuthorityPrefix("ROLE_");
        roleConverter.setAuthoritiesClaimName("scp"); // Đảm bảo claim name là 'scp'

        JwtAuthenticationConverter customConverter = new JwtAuthenticationConverter();

        customConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String email = jwt.getSubject();
            if (email == null) {
                return new HashSet<>();
            }

            Collection<GrantedAuthority> roles = roleConverter.convert(jwt);

            Collection<GrantedAuthority> permissions = permissionService.getAuthoritiesForUser(email);

            Collection<GrantedAuthority> allAuthorities = new HashSet<>();
            allAuthorities.addAll(roles);
            allAuthorities.addAll(permissions);

            return allAuthorities;
        });

        return customConverter;
    }


    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }

    @Bean
    SecureRandom secureRandom(){
        return new SecureRandom();
    }

}
