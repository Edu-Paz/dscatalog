package com.devsuperior.aula.services;

import com.devsuperior.aula.dto.ProductDTO;
import com.devsuperior.aula.entities.Product;
import com.devsuperior.aula.repositories.CategoryRepository;
import com.devsuperior.aula.repositories.ProductRepository;
import com.devsuperior.aula.services.exceptions.DatabaseException;
import com.devsuperior.aula.services.exceptions.ResourceNotFoundException;
import com.devsuperior.aula.tests.Factory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 3L;
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));
    }

    @AfterEach
    void tearDown() {
        product.setId(null);
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {
        // Configura o mock para retornar true quando existsById for chamado com ID existente
        when(productRepository.existsById(existingId)).thenReturn(true);
        // Configura o mock para não fazer nada quando deleteById for chamado com ID existente
        doNothing().when(productRepository).deleteById(existingId);

        // Verifica que nenhuma exceção é lançada ao deletar um ID existente
        Assertions.assertDoesNotThrow(() -> {
            productService.delete(existingId);
        });
        // Verifica que deleteById foi chamado exatamente 1 vez
        verify(productRepository, times(1)).deleteById(existingId);
    }

    @Test
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
        // Configura o mock para retornar false quando existsById for chamado com ID inexistente
        when(productRepository.existsById(nonExistingId)).thenReturn(false);

        // Verifica que ResourceNotFoundException é lançada ao tentar deletar um ID inexistente
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(nonExistingId);
        });

        // Verifica que existsById foi chamado exatamente 1 vez
        verify(productRepository, times(1)).existsById(nonExistingId);
        // Verifica que deleteById NUNCA foi chamado (pois o ID não existe)
        verify(productRepository, never()).deleteById(nonExistingId);
    }

    @Test
    void deleteShouldThrowDatabaseExceptionWhenDependentId(){
        // Quando existsById é chamado com dependentId deve retornar true
        when(productRepository.existsById(dependentId)).thenReturn(true);

        // Quando chamado o deleteById com dependentId, deve lançar exceção
        doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

        // Confere se é lançada a DatabaseException ao chamado o delete
        Assertions.assertThrows(DatabaseException.class, () -> {
            productService.delete(dependentId);
        });
    }

    @Test
    void findAllPagedShouldReturnPageWhenCalled(){
        when(productRepository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageable);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.getTotalElements());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void findByIdShouldReturnProductDTOWhenIdExists(){
        when(productRepository.findById(existingId)).thenReturn(Optional.of(product));

        ProductDTO productDto = productService.findById(existingId);

        Assertions.assertNotNull(productDto);
        Assertions.assertEquals(product.getId(), productDto.getId());
        Assertions.assertEquals(product.getName(), productDto.getName());
        Assertions.assertFalse(productDto.getCategories().isEmpty());

        verify(productRepository).findById(existingId);
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){
        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(nonExistingId);
        });

        verify(productRepository).findById(nonExistingId);
    }

    @Test
    void updateShouldReturnProductDTOWhenIdExists(){
        product.setId(existingId);
        when(productRepository.getReferenceById(existingId)).thenReturn(product);
        when(productRepository.save(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product product1 = new Product(existingId, "Oi", "oi", 100.0, "ooo", Instant.now());
        ProductDTO productDto = productService.update(existingId, new ProductDTO(product1));

        Assertions.assertNotNull(productDto);
        Assertions.assertEquals(existingId, productDto.getId());
        Assertions.assertEquals("Oi", productDto.getName());

        verify(productRepository).getReferenceById(existingId);
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists(){
        when(productRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        ProductDTO dto = new ProductDTO(product);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.update(nonExistingId, dto);
        });

        verify(productRepository).getReferenceById(nonExistingId);
    }
}
