package com.myopty.catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A browsing category a frame or lens is filed under.
 *
 * <p>Only the columns the filing story reads are mapped. {@code parent_id} exists
 * in V2 and is left unmapped here, so the hierarchy a later story grows does not
 * have to be invented by this one; this story only chooses a category, it does
 * not build the tree. {@code created_at} and {@code updated_at} have column
 * defaults, so no fields exist for them.
 */
@Table("category")
public class Category {

    @Id
    private Long id;

    private String name;
    private String slug;
    private CategoryItemType itemType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public CategoryItemType getItemType() {
        return itemType;
    }

    public void setItemType(CategoryItemType itemType) {
        this.itemType = itemType;
    }
}
