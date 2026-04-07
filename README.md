# User Microservice - Authentication & User Management

Production-grade Spring Boot microservice for user authentication and management in the e-zamin P2P escrow platform. Provides JWT-based authentication with role support,compatible with deal-ms OAuth2 resource server.

## Features

### Core Capabilities
- **User Registration** - Create new user accounts with email validation
- **JWT Authentication** - Secure token-based authentication
- **Role-Based Access Control** - USER and ADMIN roles
- **Profile Management** - Update user profiles and change passwords
- **Deal-ms Integration** - JWT tokens include roles claim for seamless integration

### Technical Features
- **UUID Primary Keys** - For enhanced security and distributed system compatibility
- **Audit Trail** - CreatedAt and UpdatedAt timestamps on all entities
- **Optimistic Locking** - Version column prevents concurrent modification conflicts
- **Password Encryption** - BCrypt password hashing
- **Input Validation** - Comprehensive DTO validation
- **Exception Handling** - RFC 7807 Problem Details
- **API Documentation** - OpenAPI/Swagger UI
- **Health Checks** - Spring Boot Actuator
- **Database Migrations** - Liquibase for schema versioning

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.3**
- **PostgreSQL 16**
- **JJWT 0.12.6** (JWT library)
- **Liquibase** (database migrations)
- **Gradle** (build tool)
- **Docker** (containerization)

## Prerequisites

- JDK 21+
- PostgreSQL 16+ (or use Docker Compose)
- Gradle 8+ (or use included wrapper)
- Docker & Docker Compose (optional, for containerized setup)

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Build the application
./gradlew clean build

# Start PostgreSQL and user-ms
docker-compose up -d

# View logs
docker-compose logs -f user-ms

# Access Swagger UI
open http://localhost:8081/swagger-ui.html
```

### Local Development

```bash
# Start PostgreSQL
docker run -d \
  --name user-postgres \
  -e POSTGRES_DB=user_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=user_db
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=change-this-secret-key-in-production-it-must-be-at-least-32-characters-long
export JWT_ISSUER=http://localhost:8081

# Run the application
./gradlew bootRun
```

## Configuration

Key configuration options in `application.yaml`:

```yaml
jwt:
  secret: ${JWT_SECRET}                    # JWT signing secret (min 32 chars)
  access-token-expiry-minutes: 60          # Access token expiry (default 1 hour)
  refresh-token-expiry-minutes: 1440       # Refresh token expiry (default 24 hours)
  issuer: http://localhost:8081            # JWT issuer URI
  audience: deal-ms                         # JWT audience (for deal-ms)
```

## API Endpoints

### Public Endpoints

#### Register
```bash
POST /auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123"
}
```

Response:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

#### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePassword123"
}
```

### Protected Endpoints (Require JWT)

#### Get Current User Profile
```bash
GET /users/me
Authorization: Bearer {accessToken}
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "emailVerified": false,
  "roles": ["USER"],
  "createdAt": "2026-01-30T07:30:00Z",
  "updatedAt": "2026-01-30T07:30:00Z"
}
```

#### Get User by ID
```bash
GET /users/{userId}
Authorization: Bearer {accessToken}
```

#### Update Profile
```bash
PUT /users/me/profile
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "fullName": "John Smith"
}
```

#### Change Password
```bash
POST /users/me/change-password
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "currentPassword": "SecurePassword123",
  "newPassword": "NewSecurePassword456"
}
```

## JWT Token Format

Tokens include the following claims for deal-ms compatibility:

```json
{
  "sub": "user-email@example.com",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["USER", "ADMIN"],
  "iss": "http://localhost:8081",
  "aud": "deal-ms",
  "iat": 1706600000,
  "exp": 1706603600
}
```

The **roles** claim is an array that deal-ms SecurityConfig reads to authorize requests.

## Database Schema

**3 Tables:**

1. **users** - User accounts (UUID id, email, password, status, audit fields)
2. **roles** - Available roles (UUID id, name, description, audit fields)
3. **user_roles** - Many-to-many junction table

**Key Constraints:**
- Unique email on users
- Unique role name on roles
- Composite primary key on user_roles

## Security

### Password Requirements
- Minimum 8 characters
- BCrypt encryption

### JWT Security
- SHA-256 hashed secret key
- Configurable expiry times
- Bearer token authentication
- Stateless sessions

### CORS Configuration
Allows requests from:
- http://localhost:3000 (frontend)
- http://localhost:8080 (services)
- http://localhost:8082 (deal-ms)

## Integration with deal-ms

The user-ms generates JWT tokens that deal-ms can validate:

1. **deal-ms configuration** (`application.yaml`):
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8081
```

2. **deal-ms extracts roles** from the `roles` claim in `SecurityConfig`:
```java
private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
    return jwt -> {
        List<String> roles = jwt.getClaimAsStringList("roles");
        // ... converts to authorities
    };
}
```

3. **User flow**:
   - User registers/logs in to user-ms → receives JWT
   - User calls deal-ms with JWT in Authorization header
   - deal-ms validates JWT and extracts userId & roles
   - deal-ms authorizes based on roles

## Health Check

```bash
curl http://localhost:8081/actuator/health
```

## API Documentation

Access interactive API documentation:
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## Testing

```bash
# Run all tests
./gradlew test

# Run with Testcontainers
./gradlew integrationTest

# Run specific test class
./gradlew test --tests "az.user_ms.service.UserServiceTest"
```

## Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# View PostgreSQL logs
docker logs user-ms-postgres

# Connect to database
psql -h localhost -U postgres -d user_db
```

### JWT Issues
- Ensure `JWT_SECRET` is at least 32 characters
- Check token expiry times
- Verify deal-ms uses correct issuer URI

### Application Logs
```bash
# View user-ms logs
docker logs user-ms -f

# Local development logs
./gradlew bootRun --info
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | 8081 |
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 5432 |
| `DB_NAME` | Database name | user_db |
| `DB_USER` | Database user | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `JWT_SECRET` | JWT signing secret | (required) |
| `JWT_ACCESS_EXPIRY` | Access token expiry (minutes) | 60 |
| `JWT_REFRESH_EXPIRY` | Refresh token expiry (minutes) | 1440 |
| `JWT_ISSUER` | JWT issuer URI | http://localhost:8081 |
| `JWT_AUDIENCE` | JWT audience | deal-ms |

## License

Proprietary - All rights reserved

## Support

For issues or questions, please contact the development team.
