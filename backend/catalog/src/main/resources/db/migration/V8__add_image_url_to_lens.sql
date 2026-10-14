-- Add image_url column to lens for storefront display
ALTER TABLE lens
  ADD COLUMN image_url VARCHAR(500) NULL;