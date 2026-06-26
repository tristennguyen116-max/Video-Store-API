# 🎮 Video Game Store — E-Commerce REST API

> Capstone 3 · A full-featured e-commerce backend for an online video game store, built with **Java** and **Spring Boot**, backed by a **MySQL** database and secured with **JWT** authentication.

This API powers a complete online storefront: browsing a game catalog, filtering and searching products, managing categories, adding items to a shopping cart, editing a user profile, and checking out to create an order. It serves a JavaScript front-end and is fully testable through Swagger and an Insomnia test suite.

---

## 📑 Table of Contents
- [Tech Stack](#-tech-stack)
- [Features & Requirements Met](#-features--requirements-met)
- [Screenshots](#-screenshots)
- [Interesting Code](#-interesting-code)
- [API Endpoints](#-api-endpoints)
- [How to Run It](#-how-to-run-it)
- [Testing](#-testing)
- [What I Learned (Challenges & Wins)](#-what-i-learned-challenges--wins)
- [Future Improvements](#-future-improvements)

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot (Spring Web, Spring Data JPA, Spring Security) |
| Database | MySQL |
| ORM | Hibernate / JPA |
| Auth | JWT (JSON Web Tokens), BCrypt password hashing |
| Docs | Swagger UI / OpenAPI |
| Testing | Insomnia test collection, JUnit + Mockito (unit test) |
| Build | Maven |

The project follows a clean, layered architecture:

```
Browser / Insomnia  →  Controller  →  Service  →  Repository  →  MySQL
   (HTTP request)       (the door)    (the logic)  (data access)
```

---

## ✅ Features & Requirements Met

### Phase 1 — Categories (Required) ✔
Implemented full CRUD for product categories in `CategoriesController` and `CategoryService`:
- `GET /categories` — list all categories (public)
- `GET /categories/{id}` — get one category, returns **404** when it doesn't exist (public)
- `GET /categories/{id}/products` — list all products in a category (public)
- `POST /categories` — create a category, returns **201 Created** (**admin only**)
- `PUT /categories/{id}` — update a category, returns **200 OK** (**admin only**)
- `DELETE /categories/{id}` — delete a category, returns **204 No Content** (**admin only**)

Security is enforced per-method with `@PreAuthorize("hasRole('ROLE_ADMIN')")`, so non-admins receive **403 Forbidden** and unauthenticated requests receive **401 Unauthorized**.

### Phase 2 — Bug Fixes (Required) ✔

**🐞 Bug 1 — Products disappearing from search.** `ProductService.search()` contained a stray filter that silently dropped every non-featured product from *every* search. **Fix:** removed the rogue `.filter(Product::isFeatured)` line so the catalog returns all matching products.

**🐞 Bug 2 — Stock not saving on update.** `ProductService.update()` copied every product field *except* `stock`, so editing a product's stock returned `200 OK` but never actually persisted. **Fix:** added the missing `existing.setStock(product.getStock());` line.

Both fixes are demonstrated in the test suite (the "complete product list" and "stock was saved" tests now pass), and Bug 1 is also covered by a JUnit + Mockito unit test.

### Phase 3 — Shopping Cart (Optional) ✔
Logged-in users get a database-backed cart via `ShoppingCartController` / `ShoppingCartService`:
- `GET /cart` — view the current user's cart with a calculated total
- `POST /cart/products/{id}` — add a product (or increment quantity if already present)
- `PUT /cart/products/{id}` — set the quantity of a product
- `DELETE /cart` — empty the cart (uses `@Transactional`)

### Phase 4 — User Profile (Optional) ✔
Added profile retrieval and updating in `ProfileService` plus a new `ProfileController`:
- `GET /profile` — view the logged-in user's profile
- `PUT /profile` — update the profile

### Phase 5 — Checkout / Orders (Optional) ✔
Built from scratch: `Order` and `OrderLineItem` models, their repositories, `OrderService`, and `OrdersController`:
- `POST /orders` — converts the user's cart into an order, creates a line item per cart product, clears the cart, and returns the order. Returns **400** on an empty cart. The whole operation is `@Transactional` (all-or-nothing).

### Cross-cutting requirements ✔
- **JWT authentication** with role-based access (`ROLE_USER` / `ROLE_ADMIN`)
- **`@CrossOrigin`** on every controller so the front-end can call the API
- **Correct HTTP status codes** throughout (200 / 201 / 204 / 400 / 401 / 403 / 404)
- **GitHub repo, project board, and issue tracking** for managing the work

---

## 📸 Screenshots

> 📂 The images below live in the `screenshots/` folder included with this README. Keep that folder next to `README.md` in your repo and they'll render automatically on GitHub.

### The storefront — running live on the API
The video game store front-end, logged in as an admin, with its catalog, prices, cart, and filters all served by this API. Note that **non-featured games like Halo Infinite and Spider-Man: Miles Morales are visible** — that's the Bug 1 fix in action (before the fix, every non-featured game was hidden from the store).

![Video Game Store front-end](screenshots/storefront.png)

### Project structure — the layered architecture
Every controller, model, repository, and service that makes up the API — including the files built from scratch for the optional Cart, Profile, and Orders phases.

![Project structure in IntelliJ](screenshots/project-structure.png)

### The application running
A clean startup: `Started ECommerceApplication`, connected to MySQL, with all JPA repositories detected.

![Spring Boot application running](screenshots/app-running.png)

### Swagger UI — the full API surface
Interactive OpenAPI documentation for every endpoint group: Authentication, Products, Categories, Shopping Cart, Profile, and Orders.

![Swagger UI overview](screenshots/swagger-overview.png)

### JWT authentication — admin login returning a token
`POST /login` as an admin returns **200 OK** with a JWT Bearer token and the user's role, which is then used to authorize protected requests.

![Swagger admin login returning a JWT](screenshots/swagger-login.png)

### Automated testing in Insomnia
Running the test collection against the API. The suite exercises every endpoint, including success, not-found, and security (401/403) cases.

![Insomnia test run](screenshots/insomnia-results.png)

### The Profile endpoint in Swagger
The `/profile` controller, one of the optional Phase 4 features, exposed and documented in the API.

![Profile endpoint in Swagger](screenshots/swagger-profile.png)

---

## 💡 Interesting Code

The single most satisfying fix was **Bug 1**, in `ProductService.search()`. A single stray line was hiding most of the product catalog from customers:

```java
public List<Product> search(Integer categoryId, Double minPrice, Double maxPrice, String subCategory)
{
    List<Product> products = categoryId != null
            ? productRepository.findByCategoryId(categoryId)
            : productRepository.findAll();

    return products.stream()
                   .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                   .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                   .filter(p -> subCategory == null || subCategory.equalsIgnoreCase(p.getSubCategory()))
                   // .filter(Product::isFeatured)  ← THE BUG: this ran on EVERY search,
                   //                                  silently dropping all non-featured products
                   .toList();
}
```

**How I found it:** I compared the product count in the database (60+) against what `GET /products` returned (far fewer), then noticed every product that *did* come back was marked `featured = true`. That pattern pointed straight at the search filter. Removing one line restored the entire catalog.

**Why it's a good example:** each remaining filter is written defensively (`param == null || ...`), so a missing parameter means "don't filter" rather than "return nothing" — exactly how an optional search filter should behave. I also wrote a unit test that fails with the bug present and passes once it's removed, locking the fix in place.

I'm also proud of the **checkout** logic in `OrderService`, which wraps order creation, line-item creation, and cart-clearing in a single `@Transactional` method so a customer can never end up with a half-created order.

### Code highlights

The `Order` JPA entity, mapping the Java object to the `orders` table:

![Order entity code](screenshots/order-entity-code.png)

The `OrdersController` checkout endpoint — restricted to authenticated users and returning **201 Created**:

![Orders controller code](screenshots/orders-controller-code.png)

---

## 🔌 API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/register` | Public | Create a new user account |
| POST | `/login` | Public | Authenticate and receive a JWT token |
| GET | `/products` | Public | Search/list products (filters: `cat`, `minPrice`, `maxPrice`, `subCategory`) |
| GET | `/products/{id}` | Public | Get a single product |
| POST | `/products` | Admin | Create a product |
| PUT | `/products/{id}` | Admin | Update a product |
| DELETE | `/products/{id}` | Admin | Delete a product |
| GET | `/categories` | Public | List all categories |
| GET | `/categories/{id}` | Public | Get a category |
| GET | `/categories/{id}/products` | Public | List products in a category |
| POST | `/categories` | Admin | Create a category |
| PUT | `/categories/{id}` | Admin | Update a category |
| DELETE | `/categories/{id}` | Admin | Delete a category |
| GET | `/cart` | User | View the shopping cart |
| POST | `/cart/products/{id}` | User | Add a product to the cart |
| PUT | `/cart/products/{id}` | User | Update a product's quantity |
| DELETE | `/cart` | User | Empty the cart |
| GET | `/profile` | User | View profile |
| PUT | `/profile` | User | Update profile |
| POST | `/orders` | User | Check out — create an order from the cart |

**Demo accounts** (all use the password `password`): `admin` (ROLE_ADMIN), `user` (ROLE_USER), `george` (ROLE_USER).

---

## 🚀 How to Run It

### Prerequisites
- Java JDK 17
- MySQL Server + MySQL Workbench
- IntelliJ IDEA
- Maven (bundled with IntelliJ)

### Steps
1. **Clone the repo** and open the project folder in IntelliJ. Let Maven finish downloading dependencies.
2. **Create the database.** In MySQL Workbench, open and fully execute `database/create_database_videogamestore.sql`. This builds the `videogamestore` schema with all tables and seed data (3 categories, 60+ games, demo users).
3. **Configure the connection.** In `src/main/resources/application.properties`, point the datasource at your database and credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/videogamestore
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
4. **Run the app.** Start `ECommerceApplication`. When the log shows `Started ECommerceApplication`, the API is live on **http://localhost:8080**.
5. **Open Swagger** at `http://localhost:8080/swagger-ui/index.html` to explore and test the endpoints.

> 🔒 **Note:** the committed `application.properties` uses placeholder credentials. Use your own local MySQL username/password — don't commit real passwords to a public repo.

---

## 🧪 Testing

The API was tested two ways:

- **Swagger UI** — for hands-on, single-endpoint testing. Log in via `POST /login`, click **Authorize**, paste the returned token, then exercise the protected endpoints.
- **Insomnia test collection** — an automated suite of 142 assertions covering every endpoint, including success cases, not-found cases, and security cases (401/403).

**Result: 135 / 142 assertions passing**, up from **29 / 142** at the start. All **required** functionality (Phase 1 Categories + both Phase 2 bug fixes) passes, and the optional Cart, Profile, and Checkout phases pass as well. The handful of remaining assertions are count/ordering checks in the category suite that are sensitive to test-run state.

A unit test (`ProductServiceTest`) uses JUnit and Mockito to verify the Bug 1 fix in isolation — it confirms that a search with no filters returns both featured and non-featured products.

---

## 📚 What I Learned (Challenges & Wins)

This project was my first time building a full REST API, and most of the learning came from debugging real errors. Here's an honest account.

### Challenges I worked through

- **Putting code in the right file.** Early on I pasted controller code into the service file, which broke the build with a wall of "cannot resolve symbol" errors. **Lesson:** a file's `public class` name and its `package` line must match the filename and folder. Once I lined those up, the errors vanished.

- **Brace mismatches.** An extra `}` after a method closed the class early, producing dozens of `class, interface, enum, or record expected` errors. **Lesson:** that specific error almost always means a brace is misplaced — IntelliJ highlights the matching brace when you click one, which made it easy to spot.

- **Import collisions (the trickiest one).** Names like `Order` and `Id` exist in more than one library. IntelliJ auto-imported `org.springframework.core.annotation.Order` and `org.springframework.data.annotation.Id` instead of the JPA versions, which made Hibernate insist my entity had "no identifier." **Lesson:** JPA entities must import from `jakarta.persistence` — a single `import jakarta.persistence.*;` fixed it.

- **Forgetting to restart.** Spring Boot doesn't pick up code changes until you stop and re-run the app, so several times my fixes "didn't work" simply because the old code was still running. **Lesson:** Stop → (Rebuild) → Run after every change.

- **Database state.** Leftover rows from earlier test runs threw off count-based tests, and connecting to an empty/incorrect schema caused "table doesn't exist" errors. **Lesson:** re-running the SQL script gives a clean, known starting point.

- **Understanding 401 vs 403.** It took a bit to internalize that **401 = not logged in** and **403 = logged in but not allowed**, and that this is enforced by `@PreAuthorize` *before* my controller code runs.

- **Request details matter.** Small things — hitting `/register` vs `/registration`, including the required `confirmPassword` and `role` fields, and using a unique username — were the difference between `201` and `400`.

### What worked well
- **The layered architecture** made the codebase easy to reason about: HTTP concerns in controllers, logic in services, data access in repositories.
- **Reading stack traces** — learning to look at the *first* line and the file/line number it names — turned scary red walls of text into precise clues.
- **Swagger** was invaluable for quickly confirming an endpoint worked (and for seeing exactly which status code came back).
- **Spring Data JPA** generated all the standard SQL for me, so I rarely had to write queries by hand.
- **`@Transactional`** gave me safe, all-or-nothing checkout behavior with a single annotation.

---

## 🔭 Future Improvements
- **Order history** — an endpoint for users to view their past orders and line items.
- **Inventory management** — decrement product stock automatically at checkout.
- **Input validation** with clearer, user-friendly error messages.
- **Pagination** on the products endpoint for large catalogs.
- **More unit and integration tests** across the service layer.

---

*Built as Capstone 3 for the Year Up application-development track.* 🎮
