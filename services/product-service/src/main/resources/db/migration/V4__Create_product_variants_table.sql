-- Support for size, color, material variants
CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL, -- e.g., "Red - Large"
    price DECIMAL(10,2), -- Override parent price if needed
    cost_price DECIMAL(10,2),
    weight DECIMAL(8,3),
    attributes JSONB, -- {color: "red", size: "L", material: "cotton"}
    is_default BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_variants_attributes ON product_variants USING GIN(attributes);
