# 🛠 ITech Vendor Marketplace Backend

A Spring Boot backend for managing vendor onboarding, product listings, GST/PAN verification, OTP-based authentication, and lead ranking.

---

## 🚀 Features

- OTP-based registration (Phone & Email)
- JWT-based authentication
- Vendor ranking by type (Diamond, Platinum, Gold, Basic)
- PAN/GST verification (Quicko API ready)
- Product & Category management
- Admin dashboard & role-based access
- Contact Us integration

---

## 🧰 Tech Stack

- Java 17
- Spring Boot 3
- Spring Security (JWT)
- MySQL or PostgreSQL
- Lombok
- Maven

---

## 📁 Folder Structure

```
src/main/java/com/itech/itech_backend/
├── config/
├── controller/
├── dto/
├── enums/
├── filter/
├── model/
├── repository/
├── service/
├── util/
```

---

## ⚙️ Setup Instructions

1. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/itech-backend.git
   ```

2. **Configure Database**
    - Set DB credentials in `application.properties`

3. **Run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **API Docs**
    - Import Postman collection (soon)
    - Base URL: `http://localhost:8080`

---

## 🔐 Authentication Flow

1. `POST /auth/register`  
   → Send name, email, phone  
   → OTP sent to both

2. `POST /auth/verify`  
   → Verify OTP sent to email or phone  
   → JWT is returned

Use that JWT for all authenticated routes (`Bearer <token>` in headers).

---

## 🧪 Sample Test Cases

### ✅ AuthController

- `POST /auth/register`  
  ✔️ Check OTP is generated  
  ✔️ Validate email/phone are saved
- `POST /auth/verify`  
  ✔️ Valid OTP → 200  
  ✔️ Invalid OTP → 403

### ✅ ProductController

- `POST /products`  
  ✔️ Add product with valid vendor & category  
  ❌ Missing fields → 400

- `GET /products/vendor/{id}`  
  ✔️ Returns all products by vendor

### ✅ AdminController

- `GET /admin/vendors`  
  ✔️ Returns all vendors  
  ❌ Role not ADMIN → 403

- `PUT /admin/vendor/{id}/type?vendorType=DIAMOND`  
  ✔️ Updates vendor type

### ✅ PAN/GST Verification

- `POST /tax/verify-pan`  
  ✔️ Saves PAN/GST  
  ✔️ Links to vendor ID

### ✅ Vendor Dashboard

- `GET /vendor/{id}/ranking`  
  ✔️ Returns vendor's rank/performance

### ✅ Contact Message

- `POST /contact`  
  ✔️ Saves user's message

---

## 🧪 Test Coverage Recommendation

Use:
- `SpringBootTest` for integration tests
- `MockMvc` for controller tests
- JUnit5 + Mockito for unit tests

Sample Test Class:
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean AuthService authService;

    @Test
    void shouldRegisterWithOtp() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("Test", "test@email.com", "9999999999");
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(dto)))
            .andExpect(status().isOk());
    }
}
```

---

## 👑 Roles & Vendor Types

| Role     | Access                        |
|----------|-------------------------------|
| USER     | Products, profile, dashboard  |
| ADMIN    | View/update vendors           |

| Vendor Type | Description              |
|-------------|--------------------------|
| DIAMOND     | Top-tier premium vendor  |
| PLATINUM    | Premium vendor           |
| GOLD        | Mid-tier vendor          |
| BASIC       | Default user/vendor      |

---

## 👨‍💻 Developed By

**Dipanshu Kumar Pandey**  
B.Tech - Mangalmay Institute of Engineering and Technology  
Email: dkpandeya12@gmail.com

---