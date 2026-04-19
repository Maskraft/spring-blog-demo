package com.learn.blog.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.blog.dto.LoginRequest;
import com.learn.blog.dto.RegisterRequest;
import com.learn.blog.dto.UserResponse;
import com.learn.blog.entity.Role;
import com.learn.blog.entity.User;
import com.learn.blog.exception.UsernameAlreadyExistsException;
import com.learn.blog.repository.UserRepository;

// 認証関連のビジネスサービス。登録・ログイン・現在ユーザー取得を提供する。
// ログアウト自体は Spring Security のフィルタが直接処理するためここには含めない
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    // Session に SecurityContext を明示的に保存するため (Spring Security 6 以降は手動保存が必要)
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // /register から呼ばれる。常に USER ロールで作成する (ADMIN は昇格させない)
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        User user =
                new User(request.username(), passwordEncoder.encode(request.password()), Role.USER);
        return UserResponse.from(userRepository.save(user));
    }

    // /login から呼ばれる。認証・セッション固定攻撃対策・SecurityContext 保存・CSRF トークン発行までを担う。
    // HTTP レイヤ依存の引数（HttpServletRequest/Response）を受け取るが、セッション + Cookie 方式の
    // 認証はその性質上サービス層でも HTTP 文脈を扱う必要があるため、ここに集約する
    public UserResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(), request.password()));

        // セッション固定攻撃対策：認証後に新しいセッション ID を発行する
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        httpRequest.getSession(true);

        // SecurityContext をセッションに保存 (Spring Security 6 以降は明示必要)
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        // CSRF トークンを強制的に生成して Cookie に載せる。
        // CookieCsrfTokenRepository は遅延生成のため、getToken() を呼ばないと Cookie が発行されず
        // ログイン直後の最初の書き込み API で 403 になる
        CsrfToken csrfToken = (CsrfToken) httpRequest.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        return findByUsername(authentication.getName());
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalStateException("認証済みユーザーが DB に存在しません: " + username));
    }
}
