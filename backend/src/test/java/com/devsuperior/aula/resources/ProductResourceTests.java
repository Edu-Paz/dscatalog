package com.devsuperior.aula.resources;

import com.devsuperior.aula.dto.ProductDTO;
import com.devsuperior.aula.services.ProductService;
import com.devsuperior.aula.services.exceptions.DatabaseException;
import com.devsuperior.aula.services.exceptions.ResourceNotFoundException;
import com.devsuperior.aula.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(productDTO));


    }

    @Test
    void findAllShouldReturnPage() throws Exception {
        when(productService.findAllPaged(any())).thenReturn(page);
        ResultActions resultActions = mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void findByIdShouldReturnProductWhenIdExists() throws Exception {
        productDTO.setId(1L);
        when(productService.findById(existingId)).thenReturn(productDTO);

        ResultActions resultActions = mockMvc.perform(get("/products/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").exists());
        resultActions.andExpect(jsonPath("$.name").exists());
        resultActions.andExpect(jsonPath("$.description").exists());

        productDTO.setId(null);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        when(productService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(get("/products/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isNotFound());

    }

    @Test
    void updateShouldReturnProductDTOWhenIdExists() throws Exception {
        productDTO.setId(1L);
        when(productService.update(eq(existingId), any())).thenReturn(productDTO);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(put("/products/{id}", existingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").exists());
        resultActions.andExpect(jsonPath("$.name").exists());
        resultActions.andExpect(jsonPath("$.description").exists());

        productDTO.setId(null);

    }

    @Test
    void updateShouldReturnProductDTOWhenIdDoesNotExists() throws Exception {
        when(productService.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(put("/products/{id}", nonExistingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        doNothing().when(productService).delete(existingId);

        ResultActions resultActions = mockMvc.perform(delete("/products/{id}", existingId));

        resultActions.andExpect(status().isNoContent());
        verify(productService, times(1)).delete(existingId);
    }

    @Test
    void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        doThrow(ResourceNotFoundException.class).when(productService).delete(nonExistingId);

        mockMvc.perform(delete("/products/{id}", nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource not found"));

        verify(productService, times(1)).delete(nonExistingId);
    }

    @Test
    void deleteShouldReturnDatabaseIntegrityViolationExceptionWhenIdIsDependent() throws Exception {
        doThrow(DatabaseException.class).when(productService).delete(dependentId);

        mockMvc.perform(delete("/products/{id}", dependentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Database Exception"));

        verify(productService, times(1)).delete(dependentId);
    }

    @Test
    void insertShouldReturnCreatedAndProductDTOWhenDataIsCorrect() throws Exception{
        when(productService.insert(any(ProductDTO.class))).thenReturn(productDTO);

        productDTO.setId(1L);

        String jsonBody = objectMapper.writeValueAsString(productDTO);
        mockMvc.perform(post("/products")
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists());

        verify(productService, times(1)).insert(any(ProductDTO.class));

        productDTO.setId(null);
    }

}
