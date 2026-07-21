# Resource Server

Demo Spring Boot OAuth2 Resource Server with Keycloak JWT validation.

## Quick Start

### 1. Start Keycloak

```bash
cd local/infra
docker compose up -d keycloak
```

Keycloak Admin Console: http://localhost:8180/admin
- Login: `admin` / `admin`

### 2. Configure Keycloak

#### Create Realm

1. Open http://localhost:8180/admin
2. Click the dropdown in the top-left (currently says "Master")
3. Click **Create realm**
4. Name: `rcrs`
5. Click **Create**

#### Create Client

1. In the `rcrs` realm, go to **Clients** > **Create client**
2. Client type: `OpenID Connect`
3. Client ID: `rcrs-resource-server`
4. Click **Next**
5. Client authentication: **Off** (public client for testing)
6. Valid redirect URIs: `http://localhost:8085/*`
7. Click **Next** > **Save**

#### Create User (for testing)

1. Go to **Users** > **Create new user**
2. Username: `testuser`
3. Click **Create**
4. Go to **Credentials** tab > **Set password**
5. Password: `testpass`, Temporary: **Off**
6. Click **Save**

### 3. Start Resource Server

```bash
mvn spring-boot:run -pl resource-server
```

### 4. Get JWT Token

```bash
# Using client credentials (if you created a confidential client)
TOKEN=$(curl -s -X POST http://localhost:8180/realms/rcrs/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=rcrs-resource-server" \
  -d "client_secret=<your-client-secret>" | jq -r '.access_token')

# Using password grant (for public client with testuser)
TOKEN=$(curl -s -X POST http://localhost:8180/realms/rcrs/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=rcrs-resource-server" \
  -d "username=testuser" \
  -d "password=testpass" | jq -r '.access_token')
```

### 5. Call Resource Server

```bash
# Without token (should return 401)
curl http://localhost:8085/api/test

# With valid token (should return "Resource server is secured")
curl -H "Authorization: Bearer $TOKEN" http://localhost:8085/api/test
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8085` | Server port |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:8180/realms/rcrs` | Keycloak issuer URI |

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/test` | JWT required | Returns secured message |
