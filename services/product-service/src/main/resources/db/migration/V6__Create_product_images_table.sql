-- Product images with CDN optimization
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(id) ON DELETE CASCADE,
    image_type VARCHAR(20) NOT NULL, -- 'thumbnail', 'medium', 'large', 'original'
    file_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL, -- S3 object key
    s3_bucket VARCHAR(100) NOT NULL,
    cdn_url VARCHAR(500) NOT NULL, -- CloudFront URL
    alt_text VARCHAR(255),
    file_size INTEGER, -- Bytes
    width INTEGER,
    height INTEGER,
    mime_type VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_type ON product_images(image_type);
CREATE INDEX idx_product_images_primary ON product_images(is_primary) WHERE is_primary = TRUE;
