package com.example.service;

import com.example.persistence.ProductRepository;
import com.example.persistence.model.Product;
import com.example.rest.model.ProductRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;


@ApplicationScoped
public class ProductService implements PanacheRepository<Product> {

    public static final String PATH = "C:/Users/aless/WebstormProjects/project-work-gruppo4-frontend/public/prodotti/";
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
        boolean exists = false;
        if (ingredientListId != null) {
            exists = repository.productExists(
                    productReq.getName(),
                    productReq.getPrice(),
                    ingredientListId.toHexString(),
                    productReq.getCategory()
            );
        }
        // If the product already exists Admin can't add it
        if (exists) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Impossibile aggiungere questo prodotto perchè esiste già.")
                    .type("text/plain")
                    .build();
        }
        // Create a new product to persist
        Product newProduct = new Product();
        newProduct.setName(productReq.getName());
        newProduct.setDescription(productReq.getDescription());
        newProduct.setPrice(productReq.getPrice());
        newProduct.setQuantity(productReq.getQuantity());
        if (ingredientListId != null) {
            newProduct.setIngredientListId(ingredientListId.toHexString());
        }
        newProduct.setCategory(productReq.getCategory());
        newProduct.setShowToUser(productReq.getShowToUser());

        // Save image
        // Get image base64-encoded from frontend
        String base64Image = productReq.getImage();
        if (base64Image != null && !base64Image.isEmpty()) {
            // Replace spaces with underscore
            String fileName = productReq.getName().replaceAll("\\s+", "_") + ".png";

            try {
                saveImage(base64Image, fileName);
                newProduct.setImage(PATH + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Errore durante il salvataggio dell'immagine: " + e.getMessage())
                        .build();
            }
        }

        try {
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
    public Response modifyProduct(Integer productId, ProductRequest productRequest){
        // Found the product
        Product foundProduct = findById(Long.valueOf(productId));

        if (foundProduct == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Prodotto non trovato")
                    .build();
        }

        // Update the product
        foundProduct.setName(productRequest.getName());
        foundProduct.setDescription(productRequest.getDescription());
        foundProduct.setPrice(productRequest.getPrice());
        foundProduct.setQuantity(productRequest.getQuantity());
        foundProduct.setIngredientListId(String.valueOf(addIngredientList(productRequest.getIngredientList())));
        foundProduct.setCategory(productRequest.getCategory());
        foundProduct.setImage(productRequest.getImage());
        foundProduct.setShowToUser(productRequest.getShowToUser());

        persist(foundProduct);

        return Response.ok()
                .entity("Prodotto aggiornato")
                .build();
    }

    @Transactional
    public Response removeProduct(Product product) {
        Product foundProduct = repository.findById(Long.valueOf(product.getId()));
        if (foundProduct == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Prodotto non trovato")
                    .build();
        }
        // Before deleting the product get its ingredientListId and image
        String foundProductIngredientListId = foundProduct.getIngredientListId();
        String foundProductImage = foundProduct.getImage();

        repository.deleteById(Long.valueOf(foundProduct.getId()));

        // If the deleted product is the only one that have a certain list of ingredients, delete this list too
        Product productWithSameIngredientListId = repository.find("ingredientListId", foundProductIngredientListId).firstResult();
        if (productWithSameIngredientListId == null) {
            ingredientListService.removeIngredientList(new ObjectId(foundProductIngredientListId));
        }

        // Delete the image file if it exists
        File imageFile = new File(foundProductImage);
        if (imageFile.exists()) {
            if (!imageFile.delete()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Errore durante l'eliminazione dell'immagine")
                        .build();
            }
        }

        return Response.ok()
                .entity("Prodotto eliminato")
                .build();
    }



    private void saveImage(String base64Image, String fileName) throws IOException {
        // Split the string and get the encoded part
        String[] parts = base64Image.split(",");
        String imageData = parts[1];
        // Decode the image into a byte array
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        // Save the image in this path
        Path path = Paths.get(PATH + fileName);
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(imageBytes);
        }
    }
}
