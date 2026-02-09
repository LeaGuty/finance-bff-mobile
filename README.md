# Finance BFF Mobile

Microservicio **Backend For Frontend (BFF)** diseñado para la aplicación móvil de la plataforma financiera. Actúa como intermediario entre el cliente móvil y el backend principal, adaptando y simplificando las respuestas para optimizar el consumo de datos en dispositivos móviles.

## Arquitectura

```
Cliente Móvil  ──HTTPS──>  BFF Mobile (puerto 8082)  ──HTTP──>  Backend API REST (puerto 8080)
                           [Este proyecto]                       /api/v1/cuentas
                                                                 /api/v1/cuentas/{id}/transacciones
```

### Patrón BFF

El patrón BFF (Backend For Frontend) permite:
- **Agregar** datos de múltiples endpoints del backend en una sola respuesta
- **Filtrar** información innecesaria para el cliente móvil (IDs internos, datos demográficos)
- **Transformar** las respuestas en DTOs livianos optimizados para pantallas pequeñas
- **Aplicar lógica de negocio** específica para móvil (ej: solo los últimos 5 movimientos)

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje base |
| Spring Boot | 4.0.2 | Framework principal |
| Spring Security | 7.0.2 | Autenticación y autorización |
| JJWT | 0.11.5 | Generación y validación de tokens JWT |
| Lombok | - | Reducción de código boilerplate |
| java-dotenv | 5.2.2 | Carga de variables de entorno desde `.env` |
| Maven | - | Gestión de dependencias y build |

## Estructura del Proyecto

```
src/main/java/cl/duoc/finance_bff_mobile/
├── FinanceBffMobileApplication.java      # Clase principal + Bean RestTemplate
├── config/
│   └── SecurityConfig.java               # Configuración de Spring Security y usuarios
├── controller/
│   ├── AuthController.java               # Endpoint POST /auth/login (público)
│   └── FinanceMobileController.java      # Endpoint GET /bff/mobile/v1/cuentas/{id}
├── model/
│   ├── CuentaLiteDTO.java                # DTO liviano de cuenta (nombre, saldo, tipo)
│   ├── MovimientoLiteDTO.java            # DTO liviano de movimiento (fecha, tipo, monto)
│   └── ResumenMobileDTO.java             # DTO de respuesta agregada (saludo + cuenta + movimientos)
├── security/
│   ├── JwtFilter.java                    # Filtro HTTP que valida tokens JWT en cada petición
│   └── JwtUtil.java                      # Utilidad para generar, firmar y validar tokens JWT
└── service/
    ├── FinanceMobileService.java          # Interfaz del servicio BFF
    └── FinanceMobileServiceImpl.java      # Implementación: orquesta llamadas al backend
```

## Requisitos Previos

- **Java 21** o superior
- **Maven** 3.8+
- Backend API REST ejecutándose en `http://localhost:8080`

## Configuración

### 1. Variables de entorno

Crear un archivo `.env` en la raíz del proyecto:

```env
# Clave secreta para firmar tokens JWT (Base64, mínimo 512 bits para HS512)
JWT_SECRET=<clave_base64_de_al_menos_64_bytes>
```

### 2. HTTPS / SSL

El proyecto viene configurado con un keystore PKCS12 para HTTPS local. La configuración se encuentra en `src/main/resources/application.properties`:

```properties
server.port=8082
server.ssl.key-store=classpath:finance-keystore.p12
server.ssl.key-store-password=fec4a5n5
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=finance-local
server.ssl.enabled=true
```

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación se iniciará en `https://localhost:8082`.

## Endpoints

### Autenticación

#### `POST /auth/login`

Endpoint público para autenticar usuarios y obtener un token JWT.

**Request:**
```json
{
  "username": "usuario_movil",
  "password": "5678"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response (401 Unauthorized):**
```
Error: Credenciales inválidas
```

### Resumen Móvil

#### `GET /bff/mobile/v1/cuentas/{id}`

Obtiene el resumen financiero de una cuenta optimizado para móvil. Requiere autenticación JWT con rol `CLIENTE_MOVIL`.

**Headers requeridos:**
```
Authorization: Bearer <token_jwt>
```

**Response (200 OK):**
```json
{
  "saludo": "Hola Juan, aquí tienes tus últimos movimientos.",
  "cuenta": {
    "nombre": "Juan Pérez",
    "saldo": 150000.0,
    "tipo": "Ahorro"
  },
  "ultimosMovimientos": [
    {
      "fecha": "2026-02-09",
      "transaccion": "Abono",
      "monto": 50000.0
    }
  ]
}
```

## Seguridad

- **HTTPS** habilitado con certificado auto-firmado (desarrollo)
- **JWT (HS512)** para autenticación stateless
- **CSRF deshabilitado** (API stateless, no usa cookies de sesión)
- **Roles**: solo usuarios con `ROLE_CLIENTE_MOVIL` acceden a los endpoints del BFF
- **Token forwarding**: el BFF reenvía el token JWT al backend para mantener la cadena de autenticación

### Flujo de autenticación

```
1. POST /auth/login (credenciales) ──> BFF valida contra InMemoryUserDetailsManager
2. BFF genera token JWT firmado con HS512 ──> Retorna al cliente
3. GET /bff/mobile/v1/cuentas/{id} + Bearer token ──> JwtFilter valida el token
4. BFF reenvía el token al backend ──> Backend valida y responde
5. BFF filtra y transforma la respuesta ──> Retorna DTO liviano al cliente
```

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| usuario_movil | 5678 | ROLE_CLIENTE_MOVIL |

## Autores

Equipo Backend 3 - Duoc UC
