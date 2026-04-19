package com.learn.blog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

// Spring Security 設定。Session + Cookie 方式・BCrypt・CSRF Cookie トークンを有効にする。
// @PreAuthorize による認可を有効化するため @EnableMethodSecurity を付与する。
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt (strength=10)。デフォルト値で十分安全
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF：Cookie ベースで XSRF-TOKEN を発行。SPA は JS で読んで X-XSRF-TOKEN ヘッダに載せる。
                // HttpOnly=false により JS から Cookie を読める設定。
                // login / register は未認証状態のアクセスで CSRF 対策の対象外のため除外する
                // (初回リクエスト時はトークン Cookie がまだ無く、ヘッダを送れないため)。
                // CsrfTokenRequestAttributeHandler のリクエスト属性名を null にすることで
                // Spring Security 6 の BREACH 対策（トークンのマスキング）を無効化し、
                // Cookie の生値をそのまま X-XSRF-TOKEN ヘッダで送る SPA 標準パターンにする
                .csrf(
                        csrf -> {
                            CsrfTokenRequestAttributeHandler handler =
                                    new CsrfTokenRequestAttributeHandler();
                            handler.setCsrfRequestAttributeName(null);
                            csrf.csrfTokenRepository(
                                            CookieCsrfTokenRepository.withHttpOnlyFalse())
                                    .csrfTokenRequestHandler(handler)
                                    .ignoringRequestMatchers(
                                            "/api/v1/auth/login", "/api/v1/auth/register");
                        })

                // Session 方式：必要時のみ Session を作成
                .sessionManagement(
                        sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 認可ルール
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // 認証エンドポイント：未認証でもアクセス可
                                        .requestMatchers(
                                                "/api/v1/auth/register", "/api/v1/auth/login")
                                        .permitAll()
                                        // 記事の参照：公開
                                        .requestMatchers(HttpMethod.GET, "/api/v1/articles/**")
                                        .permitAll()
                                        // その他は認証必須。CRUD の ADMIN 限定は Controller 側 @PreAuthorize で担保
                                        .anyRequest()
                                        .authenticated())

                // フォームログイン・HTTP Basic は使わない（API は専用の /auth/login で受ける）
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 未認証時：リダイレクトではなく 401 を返す（SPA 向け）
                .exceptionHandling(
                        eh ->
                                eh.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // ログアウト：POST /api/v1/auth/logout でセッション破棄
                .logout(
                        logout ->
                                logout.logoutUrl("/api/v1/auth/logout")
                                        .logoutSuccessHandler(
                                                (req, res, auth) ->
                                                        res.setStatus(
                                                                HttpStatus.NO_CONTENT.value()))
                                        .deleteCookies("JSESSIONID"));

        return http.build();
    }
}
