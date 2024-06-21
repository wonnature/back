package com.wonkwang.wonnature.domain;

import com.wonkwang.wonnature.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long hit = 0L; //조회수

    private String title;
    private String englishTitle;
    private String oneLineIntroduce;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private String storeLink;

    @ElementCollection
    @OrderColumn(name = "image_url_order")
    private List<String> imageUrls;

    public Product(ProductDTO productDTO) {
        createEntity(productDTO);
    }
    public void updateProduct(ProductDTO productDTO) {
        createEntity(productDTO);
    }

    private void createEntity(ProductDTO productDTO) {
        title = productDTO.getTitle();
        storeLink = productDTO.getStoreLink();
        imageUrls = productDTO.getImageUrls();
        productType = productDTO.getProductType();
        oneLineIntroduce = productDTO.getOneLineIntroduce();
    }

    public void addHit() {
        hit += 1;
    }

}
