package com.wonkwang.wonnature.dto;

import com.wonkwang.wonnature.domain.Product;
import com.wonkwang.wonnature.domain.ProductType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProductDTO {

    private Long id;
    private Long hit;
    private String title;
    private String content;
    private String englishTitle;
    private ProductType productType;
    private String oneLineIntroduce;
    private String configuration;
    private String storeLink;
    private List<String> imageUrls;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.hit = product.getHit();
        this.title = product.getTitle();
        this.content = product.getContent();
        this.productType = product.getProductType();
        this.oneLineIntroduce = product.getOneLineIntroduce();
        this.configuration = product.getConfiguration();
        this.englishTitle = product.getEnglishTitle();
        this.storeLink = product.getStoreLink();
        this.imageUrls = product.getImageUrls();
    }
}
