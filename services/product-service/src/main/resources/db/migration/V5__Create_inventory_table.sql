-- Real-time inventory tracking
CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(id) ON DELETE CASCADE,
    location_id UUID, -- For multi-warehouse support
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0, -- For pending orders
    available_quantity INTEGER GENERATED ALWAYS AS (quantity - reserved_quantity) STORED,
    reorder_level INTEGER DEFAULT 5, -- Auto-alert threshold
    max_level INTEGER, -- Maximum stock level
    last_restocked_at TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT NOW(),
    updated_by UUID,
    UNIQUE(product_id, variant_id, location_id)
);

CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_available_qty ON inventory(available_quantity);
CREATE INDEX idx_inventory_low_stock ON inventory(available_quantity) WHERE available_quantity <= reorder_level;
