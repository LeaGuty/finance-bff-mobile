package cl.duoc.finance_bff_mobile.security;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para la generación, firma y validación de tokens JWT.
 *
 * Utiliza el algoritmo HS512 (HMAC-SHA512) para firmar los tokens.
 * La clave secreta se carga desde la variable de entorno JWT_SECRET
 * definida en el archivo .env del proyecto (codificada en Base64).
 *
 * Requisito de seguridad: la clave debe tener al menos 512 bits (64 bytes)
 * para cumplir con la especificación RFC 7518 Sección 3.2 para HS512.
 *
 * Estructura del token generado:
 * - Header:  { "alg": "HS512" }
 * - Payload: { "sub": "username", "role": "ROLE_XXX", "iat": ..., "exp": ... }
 *
 * @author Equipo Backend 3 - Duoc UC
 */
@Component
public class JwtUtil {

    // Carga las variables de entorno desde el archivo .env en la raíz del proyecto.
    // ignoreIfMissing() evita que la app falle si el archivo no existe.
    private final Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();

    // Clave secreta en Base64 leída desde .env (con valor por defecto de respaldo)
    private final String SECRET_KEY_STRING = dotenv.get("JWT_SECRET",
        "ZXN0YV9lc191bmFfY2xhdmVfbXV5X3NlZ3VyYV95X2xhcmdhX3BhcmFfY3VtcGxpcl9jb25fbG9zX3JlcXVpc2l0b3NfZGVfSFM1MTJfYmZmXzIwMjZfZHVvY19jaGlsZV9wYXJhX2VsX2V4YW1lbg==");

    // Decodifica la clave Base64 y genera un Key compatible con HMAC-SHA
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY_STRING));

    // Tiempo de expiración del token: 30 minutos (en milisegundos)
    private final long EXPIRATION_TIME = 1000 * 60 * 30;

    /**
     * Genera un token JWT firmado para el usuario autenticado.
     *
     * @param username nombre de usuario que será el "subject" del token
     * @param role     rol del usuario (ej: "ROLE_CLIENTE_MOVIL") incluido como claim
     * @return token JWT firmado como String
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * Construye y firma el token JWT con los claims, subject y tiempos de expiración.
     *
     * @param claims  mapa de claims personalizados (ej: role)
     * @param subject identificador del usuario (username)
     * @return token JWT compacto como String
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Valida que el token pertenezca al usuario indicado y no haya expirado.
     *
     * @param token    token JWT a validar
     * @param username nombre de usuario esperado
     * @return true si el token es válido y vigente, false en caso contrario
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /** Extrae el username (subject) del token JWT. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrae el rol del usuario desde los claims personalizados del token. */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /** Extrae la fecha de expiración del token JWT. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token usando una función resolutora.
     *
     * @param token          token JWT del cual extraer el claim
     * @param claimsResolver función que selecciona el claim deseado
     * @return valor del claim extraído
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Parsea el token JWT y retorna todos los claims del payload. */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }

    /** Verifica si el token ha superado su fecha de expiración. */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}