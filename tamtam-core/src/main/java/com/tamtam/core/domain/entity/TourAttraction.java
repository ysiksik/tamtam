package com.tamtam.core.domain.entity;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Entity
@Table(
    name = "tour_attractions",
    uniqueConstraints = @UniqueConstraint(name = "uk_tour_attractions_content_id", columnNames = "content_id")
)
public class TourAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false, unique = true, length = 30)
    private String contentId;

    @Column(name = "content_type_id", nullable = false, length = 10)
    private String contentTypeId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 500)
    private String addr1;

    @Column(length = 500)
    private String addr2;

    @Column(length = 20)
    private String zipcode;

    @Column(length = 100)
    private String tel;

    @Column(name = "first_image", length = 1000)
    private String firstImage;

    @Column(name = "first_image2", length = 1000)
    private String firstImage2;

    @Column(name = "map_x", precision = 18, scale = 14)
    private BigDecimal mapX;

    @Column(name = "map_y", precision = 18, scale = 14)
    private BigDecimal mapY;

    @Column(length = 10)
    private String mlevel;

    @Column(name = "area_code", length = 10)
    private String areaCode;

    @Column(name = "sigungu_code", length = 10)
    private String sigunguCode;

    @Column(name = "ldong_regn_cd", length = 10)
    private String lDongRegnCd;

    @Column(name = "ldong_regn_nm", length = 100)
    private String lDongRegnNm;

    @Column(name = "ldong_signgu_cd", length = 10)
    private String lDongSignguCd;

    @Column(name = "ldong_signgu_nm", length = 100)
    private String lDongSignguNm;

    @Column(name = "cat1", length = 20)
    private String cat1;

    @Column(name = "cat2", length = 20)
    private String cat2;

    @Column(name = "cat3", length = 20)
    private String cat3;

    @Column(name = "created_time", length = 30)
    private String createdTime;

    @Column(name = "modified_time", length = 30)
    private String modifiedTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateFrom(
        String contentTypeId,
        String title,
        String addr1,
        String addr2,
        String zipcode,
        String tel,
        String firstImage,
        String firstImage2,
        BigDecimal mapX,
        BigDecimal mapY,
        String mlevel,
        String areaCode,
        String sigunguCode,
        String lDongRegnCd,
        String lDongRegnNm,
        String lDongSignguCd,
        String lDongSignguNm,
        String cat1,
        String cat2,
        String cat3,
        String createdTime,
        String modifiedTime
    ) {
        this.contentTypeId = contentTypeId;
        this.title = title;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.zipcode = zipcode;
        this.tel = tel;
        this.firstImage = firstImage;
        this.firstImage2 = firstImage2;
        this.mapX = mapX;
        this.mapY = mapY;
        this.mlevel = mlevel;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.lDongRegnCd = lDongRegnCd;
        this.lDongRegnNm = lDongRegnNm;
        this.lDongSignguCd = lDongSignguCd;
        this.lDongSignguNm = lDongSignguNm;
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
    }
}
