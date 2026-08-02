# Laybhari Backend — MVP (Auth + Categories + Products)

## What's included
- User registration/login with JWT
- Category listing
- Product CRUD (public GET, admin-only POST/PUT/DELETE)
- Global error handling → clean JSON errors for your Flutter app to parse
- CORS enabled (loosely — tighten before production)

## Setup

1. **Install MySQL locally** (or use a cloud instance).
2. Run the schema:
   ```bash
   mysql -u root -p < schema.sql
   ```
3. Edit `src/main/resources/application.yml`:
   - Set your real MySQL password
   - Change `app.jwt.secret` to a long random string (keep it out of git in real production — use an env variable)
4. Run it:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   Or in bash / macOS / Linux:
   ```bash
   ./mvnw spring-boot:run
   ```
   Server starts on `http://localhost:8080`

## Test it (curl examples)

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@laybhari.com","password":"test123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@laybhari.com","password":"test123"}'
```
Copy the `token` from the response.

**Get products (public, no token needed):**
```bash
curl http://localhost:8080/api/products
```

**Create a product (needs an ADMIN token — see note below):**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"name":"Silk Saree","description":"Handwoven","price":2499.00,"stock":10,"categoryId":1,"imageUrl":"https://..."}'
```

**Note on admin access:** newly registered users get role `CUSTOMER`. To test admin
endpoints, manually update a user's role in MySQL:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'test@laybhari.com';
```
Then log in again to get a fresh token with the ADMIN role baked in.

## What's next (once this works end-to-end with Flutter)
1. Wire your Flutter app to `/api/auth/register`, `/api/auth/login`, `/api/products`
   — get ONE screen (Product Listing) fully working before adding more.
2. Add Cart + Orders tables/endpoints (same pattern as Products).
3. Add Razorpay integration for payments.
4. Add image upload (Cloudinary) instead of passing raw imageUrl strings.
5. Before going live: move secrets to environment variables, restrict CORS,
   add rate limiting on `/api/auth/**`.

## Why this structure
Each layer has one job:
- `entity/` → maps to MySQL tables
- `repository/` → database queries (Spring Data JPA does the SQL for you)
- `service/` → business logic (validation, rules)
- `controller/` → HTTP layer only, no logic
- `dto/` → the exact JSON shape sent to/from Flutter — this is your frontend/backend contract
- `security/` + `config/` → JWT + route protection
