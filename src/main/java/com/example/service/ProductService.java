package com.example.service;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.Product;
import com.example.rest.model.ProductRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.List;


@ApplicationScoped
public class ProductService implements PanacheRepository<Product> {

    private final ProductRepository repository;
    private final IngredientListService ingredientListService;

    public ProductService(ProductRepository repository, IngredientListService ingredientListService) {
        this.repository = repository;
        this.ingredientListService = ingredientListService;
    }


    @Transactional
    public ObjectId addIngredientList(List<String> ingredientList) {
        return ingredientListService.createIngredientList(ingredientList);
    }

    @Transactional
    public Response addProduct(ProductRequest productReq, ObjectId ingredientListId) {
        // The product already exists?
        boolean exists = repository.productExists(
                productReq.getName(),
                productReq.getPrice(),
                ingredientListId.toHexString(),
                productReq.getCategory()
        );
        // If the product already exists Admin can't add it
        if (exists) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Impossibile aggiungere questo prodotto perchè esiste già.")
                    .type("text/plain")
                    .build();
        }

        Product newProduct = new Product();
        newProduct.setName(productReq.getName());
        newProduct.setDescription(productReq.getDescription());
        newProduct.setPrice(productReq.getPrice());
        newProduct.setQuantity(productReq.getQuantity());
        newProduct.setIngredientListId(ingredientListId.toHexString());
        newProduct.setCategory(productReq.getCategory());
        newProduct.setImage(productReq.getImage());
        newProduct.setShowToUser(productReq.getShowToUser());

        try {
            System.out.println("Inserendo il prodotto ... ");
            persist(newProduct);
            return Response.ok("Il prodotto è stato aggiunto correttamente").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public Response removeProduct(Product product) {
        Product foundProduct = repository.findById(Long.valueOf(product.getId()));
        if (foundProduct == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Prodotto non trovato")
                    .build();
        }
        String foundProductIngredientListId = foundProduct.getIngredientListId();
        repository.deleteById(Long.valueOf(foundProduct.getId()));

        // If the deleted product is the only one that have a certain list of ingredients, delete this list too
        Product productWithSameIngredientListId = repository.find("ingredientListId", foundProductIngredientListId).firstResult();
        if (productWithSameIngredientListId == null) {
            ingredientListService.removeIngredientList(new ObjectId(foundProductIngredientListId));
        }

        return Response.ok()
                .entity("Prodotto eliminato")
                .build();
    }
}
