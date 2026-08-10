package com.akademi.finsight.fund.decision.converter;

import com.akademi.finsight.fund.decision.entity.AssetCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

@Converter(autoApply = true)
public class AssetCategoryConverter implements AttributeConverter<AssetCategory, String> {

    public static final String STOCK_DB = "Hisse Senedi";
    public static final String REPO_DB = "Ters-Repo";
    public static final String FUTURE_DB = "Vadeli İşl. Nakit Teminatı";
    public static final String FUND_DB = "Yatırım Fonu Katılma Payı";

    @Override
    public String convertToDatabaseColumn(AssetCategory attribute) {
        if (Objects.isNull(attribute)) {
            return null;
        }

        return switch (attribute) {
            case STOCK -> STOCK_DB;
            case REPO -> REPO_DB;
            case FUTURE -> FUTURE_DB;
            case FUND -> FUND_DB;
        };
    }

    @Override
    public AssetCategory convertToEntityAttribute(String dbData) {
        if (Objects.isNull(dbData)) {
            return null;
        }

        return switch (dbData.trim()) {
            case STOCK_DB -> AssetCategory.STOCK;
            case REPO_DB -> AssetCategory.REPO;
            case FUTURE_DB, "Vadeli İşlemler Teminat" -> AssetCategory.FUTURE;
            case FUND_DB, "Yatırım Fonları Katılma Payları" -> AssetCategory.FUND;
            default -> throw new IllegalArgumentException("Unknown asset category: " + dbData);
        };
    }
}