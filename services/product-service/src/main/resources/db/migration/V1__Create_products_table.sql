-- Products table with proper indexing
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(500) NOT NULL,
    slug VARCHAR(500) UNIQUE NOT NULL, -- For SEO URLs
    description TEXT,
    short_description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2), -- Seller's cost (private)
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, ACTIVE, INACTIVE, ARCHIVED
    weight DECIMAL(8,3), -- For shipping calculations
    dimensions JSONB, -- {length, width, height, unit}
    tags TEXT[], -- For search optimization
    seo_title VARCHAR(200),
    seo_description VARCHAR(500),
    meta_keywords TEXT[],
    featured BOOLEAN DEFAULT FALSE,
    digital_product BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID,
    updated_by UUID
);

-- Indexes for performance
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_featured ON products(featured) WHERE featured = TRUE;
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_tags ON products USING GIN(tags);
