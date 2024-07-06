package com.wonkwang.wonnature.domain;

import com.wonkwang.wonnature.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private String configuration;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private String storeLink;

    @ElementCollection
    @OrderColumn(name = "image_url_order")
    private List<String> imageUrls = new ArrayList<>();

    public Product(ProductDTO productDTO) {
        createEntity(productDTO);
    }
    public void updateProduct(ProductDTO productDTO) {
        createEntity(productDTO);
    }

    private void createEntity(ProductDTO productDTO) {
        title = productDTO.getTitle();
        content = productDTO.getContent();
        storeLink = productDTO.getStoreLink();
        imageUrls = productDTO.getImageUrls();
        productType = productDTO.getProductType();
        oneLineIntroduce = productDTO.getOneLineIntroduce();
        englishTitle = productDTO.getEnglishTitle();
        configuration = productDTO.getConfiguration();
    }

    public void addHit() {
        hit += 1;
    }

}
