-- Categories with nested set model for efficient queries
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES categories(id),
    hierarchy_path VARCHAR(1000), -- e.g., "/electronics/phones/smartphones"
    level INTEGER DEFAULT 0,
    left_bound INTEGER NOT NULL,
    right_bound INTEGER NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    seo_title VARCHAR(200),
    seo_description VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_hierarchy_path ON categories(hierarchy_path);
CREATE INDEX idx_categories_nested_set ON categories(left_bound, right_bound);
