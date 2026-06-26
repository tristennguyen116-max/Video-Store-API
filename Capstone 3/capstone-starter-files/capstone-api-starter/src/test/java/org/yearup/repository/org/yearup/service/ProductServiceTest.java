package org.yearup.repository.org.yearup.service;

import org.junit.jupiter.api.Test;
import org.yearup.models.Product;
import org.yearup.repository.ProductRepository;
import org.yearup.service.ProductService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest
{
    @Test
    void search_withNoFilters_shouldIncludeNonFeaturedProducts()
    {
        // arrange: one featured + one NON-featured product
        Product featured    = new Product(1, "Featured Game",    10.0, 1, "", "Action", 5, true,  "");
        Product notFeatured = new Product(2, "Non-Featured Game",20.0, 1, "", "Action", 5, false, "");

        // a fake repository that returns both
        ProductRepository repo = mock(ProductRepository.class);
        when(repo.findAll()).thenReturn(List.of(featured, notFeatured));

        ProductService service = new ProductService(repo);

        // act: search with no filters
        List<Product> result = service.search(null, null, null, null);

        // assert: BEFORE the fix this is 1 (bug hides the non-featured one); AFTER the fix it's 2
        assertEquals(2, result.size(), "Search must not hide non-featured products.");
        assertTrue(result.stream().anyMatch(p -> !p.isFeatured()),
                "A non-featured product should be in the results.");
    }
}