package com.devsuperior.aula.services;

import com.devsuperior.aula.dto.CategoryDTO;
import com.devsuperior.aula.entities.Category;
import com.devsuperior.aula.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll(){
        List<Category> list = categoryRepository.findAll();
        return list.stream().map(CategoryDTO::new).toList();
    }
}
