package com.devsuperior.aula.dto;

import com.devsuperior.aula.entities.Category;

import java.io.Serializable;

public class CategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryDTO(Category entity) {
        id = entity.getId();
        name = entity.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
