# Spring Security — User Registration and Login (Step-by-step guide)

This guide walks you through configuring Spring Security for user registration and login. It covers a modern Spring Boot approach (Spring Security 5/6+), shows both a session/form-based flow and a REST + JWT flow, and includes complete, copy-pasteable examples for key classes and configuration.

Target audience: developers familiar with Spring Boot, Maven, and basic JPA.

Checklist (what you'll get):
- Project dependencies and quick Maven snippets
- Database and migration recommendations
- User entity, DTOs, and repository
- Password hashing with BCrypt
- UserDetailsService and AuthenticationProvider setup
- Security configuration (SecurityFilterChain) for REST/JWT and form-login
- Registration and authentication controllers
- JWT utility (token creation/validation) for REST APIs
- Example Flyway SQL for `users` table
- Curl examples to test registration and login
- Common pitfalls and debugging tips

---

Quick decisions you can make up-front
- Session (cookie) based login for server-rendered apps (Thymeleaf): simpler; Spring manages sessions.
- JWT (stateless) for single-page apps or mobile clients: more explicit, scalable, requires token handling.

This guide gives both approaches. Use the section for the flow you want.

Prerequisites
- Spring Boot 2.7+ or 3.x (adjust imports if you're on older versions)
- JDK 17+ recommended
- A relational DB (Postgres/MySQL) and JPA/Hibernate
- Optional: Flyway or Liquibase for migrations

1) Dependencies (Maven)

Add these to your `pom.xml`:

```xml
<!-- web, security, JPA, validation -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<!-- DB driver example (Postgres) -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

If you plan to use JWT:

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.11.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>
```

2) Database and migration
- Create a `users` table (example Flyway SQL provided below).
- Use migrations to ensure the table exists before the application starts (prevents Schema validation errors where Hibernate expects missing tables).

Example Flyway SQL (src/main/resources/db/migration/V1__create_users.sql):

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(180) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL,
  email VARCHAR(200),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);
```

3) User entity and repository

src/main/java/.../model/User.java (JPA entity):

```java
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password; // hashed

  private String email;

  private boolean enabled = true;

  // getters/setters
}
```

Repository:

```java
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  boolean existsByUsername(String username);
}
```

4) DTOs and Registration request validation

```java
public class RegisterRequest {
  @NotBlank
  private String username;
  @NotBlank
  @Size(min = 8)
  private String password;
  @Email
  private String email;
  // getters/setters
}
```

5) Password hashing

Define a BCryptPasswordEncoder bean:

```java
@Configuration
public class PasswordConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

6) UserDetails and UserDetailsService

Implement a class that adapts your User entity to Spring Security's UserDetails.

```java
public class AppUserDetails implements UserDetails {
  private final User user;
  public AppUserDetails(User user) { this.user = user; }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
  @Override public String getPassword() { return user.getPassword(); }
  @Override public String getUsername() { return user.getUsername(); }
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return user.isEnabled(); }
}

@Service
public class JpaUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  public JpaUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new AppUserDetails(user);
  }
}
```

7) AuthenticationProvider and AuthenticationManager

Use DaoAuthenticationProvider wired with the PasswordEncoder and UserDetailsService.

```java
@Configuration
public class AuthProviderConfig {
  @Bean
  public DaoAuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    provider.setPasswordEncoder(encoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
```

8) Security configuration (Spring Security 5.7+ style)

a) Session / form login (server-rendered):

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf().and()
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll()
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/login")
        .defaultSuccessUrl("/", true)
        .permitAll()
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login?logout")
      );
    return http.build();
  }
}
```

b) REST + JWT (stateless):

- Use a filter that checks Authorization: Bearer <token> and sets authentication into SecurityContext.
- Disable sessions and CSRF for pure APIs (if you use cookies, keep CSRF).

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
```

9) Registration controller (REST example)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils; // if using JWT

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
    if (userRepository.existsByUsername(req.getUsername())) {
      return ResponseEntity.badRequest().body(Map.of("error","username exists"));
    }
    User user = new User();
    user.setUsername(req.getUsername());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    userRepository.save(user);
    return ResponseEntity.ok(Map.of("status","registered"));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest lr) {
    Authentication auth = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(lr.getUsername(), lr.getPassword())
    );
    SecurityContextHolder.getContext().setAuthentication(auth);
    // for JWT:
    String token = jwtUtils.generateToken(auth);
    return ResponseEntity.ok(Map.of("token", token));
  }
}
```

10) JWT utilities (brief)

- Generate token with subject=username and short expiry.
- Validate token and extract username.

(Use jjwt or Nimbus; example outline below):

```java
@Component
public class JwtUtils {
  private final Key key = Keys.hmacShaKeyFor("a-very-secret-key-that-is-long-enough...".getBytes(StandardCharsets.UTF_8));

  public String generateToken(Authentication auth) {
    String username = ((UserDetails) auth.getPrincipal()).getUsername();
    return Jwts.builder()
      .setSubject(username)
      .setIssuedAt(new Date())
      .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
      .signWith(key)
      .compact();
  }

  public String getUsernameFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build()
      .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validate(String token) {
    try { Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); return true; }
    catch (JwtException e) { return false; }
  }
}
```

JWT Authentication filter example (simplified):

```java
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtils jwtUtils;
  private final UserDetailsService userDetailsService;
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      if (jwtUtils.validate(token)) {
        String username = jwtUtils.getUsernameFromToken(token);
        UserDetails ud = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    filterChain.doFilter(request, response);
  }
}
```

11) Testing with curl

- Register:

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"alice","password":"s3cr3tPass"}'
```

- Login (get JWT):

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"alice","password":"s3cr3tPass"}'
```

- Call protected endpoint:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/protected
```

12) Common pitfalls & troubleshooting

- Schema validation failure: ensure migrations ran; supply proper DB URL and credentials; check `spring.jpa.hibernate.ddl-auto` (use `validate` with migrations, `update` for quick dev).
- Missing bean: ensure you expose a `PasswordEncoder` and `UserDetailsService`/`AuthenticationProvider` used by Spring Security.
- Password mismatch: always store hashed passwords. Use `BCryptPasswordEncoder.matches(raw, storedHash)` for checks.
- CSRF: for stateful forms keep CSRF enabled and include the token in forms. For stateless APIs, disable CSRF or manage tokens appropriately.
- CORS: when calling from a browser app, configure CORS either via Spring MVC or in security config.
- Test the user lookup independently: call `userRepository.findByUsername(...)` in a unit test.

13) Next steps and hardening

- Email verification for registrations
- Password reset flows
- Rate-limiting login attempts / account lockout
- Use refresh tokens for JWT flows
- Use HTTPS and rotate JWT signing keys
- Add roles/authorities and secure endpoints with `hasRole(...)` or method-level `@PreAuthorize`

Appendix — Minimal `application.properties` for dev

```
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=myuser
spring.datasource.password=mypass
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
logging.level.org.springframework.security=DEBUG
```

Wrap-up

This guide provided a practical path: model your `User`, use `BCryptPasswordEncoder`, implement a `UserDetailsService`, wire an authentication provider, and then pick either a session-based or a stateless JWT configuration. The included controllers and utilities can be used as a starting point and hardened for production needs (email verification, account lockout, refresh tokens, etc.).

If you'd like, I can generate a minimal working example in this repository (entities, controllers, security config, tests, and Flyway script) tailored to your project structure — tell me whether you prefer session-based or JWT-based auth and I will create the source files and run the tests.

