package com.populaire.projetguerrefroide.utils;

import java.util.Date;
import java.util.List;

public class Bookmark {
    private final String iconNameFile;
    private final String nameId;
    private final String descriptionId;
    private final Date date;
    private final List<String> countriesId;

    public Bookmark(String iconNameFile, String nameId, String descriptionId, Date date, List<String> countriesId) {
        this.iconNameFile = iconNameFile;
        this.nameId = nameId;
        this.descriptionId = descriptionId;
        this.date = date;
        this.countriesId = countriesId;
    }

    public String getIconNameFile() {
        return this.iconNameFile;
    }

    public String getNameId() {
        return this.nameId;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

    public Date getDate() {
        return this.date;
    }

    public List<String> getCountriesId() {
        return this.countriesId;
    }

    public String toString() {
        return "Bookmark{" +
                "iconNameFile='" + this.iconNameFile + '\'' +
                ", nameId='" + this.nameId + '\'' +
                ", descriptionId='" + this.descriptionId + '\'' +
                ", date=" + this.date +
                ", countriesId=" + this.countriesId +
                '}';
    }
}
