-- ============================================
-- Laybhari Vlogs - Database Schema
-- Auth + Categories + Products + Product Variants + Cart System
-- ============================================

CREATE DATABASE IF NOT EXISTS laybhari_db;
USE laybhari_db;

-- ---------- USERS ----------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password      VARCHAR(255)  NOT NULL,       -- BCrypt hash
    role          VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER', -- CUSTOMER | ADMIN
    phone         VARCHAR(15),
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ---------- CATEGORIES ----------
CREATE TABLE IF NOT EXISTS categories (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL UNIQUE,
    image_url     VARCHAR(500),
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- ---------- PRODUCTS ----------
CREATE TABLE IF NOT EXISTS products (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id   BIGINT        NOT NULL,
    name          VARCHAR(200)  NOT NULL,
    description   TEXT,
    image_url     VARCHAR(500),
    is_active     BOOLEAN       DEFAULT TRUE,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ---------- PRODUCT IMAGES ----------
CREATE TABLE IF NOT EXISTS product_images (
    product_id    BIGINT        NOT NULL,
    image_url     TEXT          NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ---------- PRODUCT VARIANTS ----------
CREATE TABLE IF NOT EXISTS product_variants (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id    BIGINT        NOT NULL,
    weight_label  VARCHAR(50)   NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    stock         INT           NOT NULL DEFAULT 0,
    is_active     BOOLEAN       DEFAULT TRUE,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ---------- CART ----------
CREATE TABLE IF NOT EXISTS cart (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL UNIQUE,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------- CART ITEMS ----------
CREATE TABLE IF NOT EXISTS cart_items (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id            BIGINT    NOT NULL,
    product_variant_id BIGINT    NOT NULL,
    quantity           INT       NOT NULL DEFAULT 1,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- ---------- COUPONS ----------
CREATE TABLE IF NOT EXISTS coupons (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(50)   NOT NULL UNIQUE,
    discount_type     VARCHAR(20)   NOT NULL, -- PERCENTAGE | FLAT
    discount_value    DECIMAL(10,2) NOT NULL,
    min_order_amount  DECIMAL(10,2),
    is_active         BOOLEAN       DEFAULT TRUE,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    expires_at        TIMESTAMP     NULL
);
