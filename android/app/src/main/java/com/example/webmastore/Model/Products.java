package com.example.webmastore.Model;

public class Products {
    private String ID, category, imageRef, name, alias;
    private int Stocks, priceMember, priceNonMember;

    public Products() {}

    public Products(String ID, String category, String imageRef, String name, int Stocks,
                    int priceMember, int priceNonMember, String alias) {
        this.ID = ID;
        this.category = category;
        this.imageRef = imageRef;
        this.name = name;
        this.Stocks = Stocks;
        this.priceMember = priceMember;
        this.priceNonMember = priceNonMember;
        this.alias = alias;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageRef() {
        return imageRef;
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStocks() {
        return Stocks;
    }

    public void setStocks(int stocks) {
        this.Stocks = stocks;
    }

    public int getPriceMember() {
        return priceMember;
    }

    public void setPriceMember(int priceMember) {
        this.priceMember = priceMember;
    }

    public int getPriceNonMember() {
        return priceNonMember;
    }

    public void setPriceNonMember(int priceNonMember) {
        this.priceNonMember = priceNonMember;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
