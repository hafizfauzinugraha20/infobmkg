package id.hafiz.infobmkg;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GempaData {
    @SerializedName("Infogempa")
    public InfoGempa infoGempa;

    public static class InfoGempa {
        @SerializedName("gempa")
        public List<Gempa> gempaList;
    }

    public static class Gempa {
        @SerializedName("Tanggal") public String tanggal;
        @SerializedName("Jam") public String jam;
        @SerializedName("Coordinates") public String coordinates;
        @SerializedName("Lintang") public String lintang;
        @SerializedName("Bujur") public String bujur;
        @SerializedName("Magnitude") public String magnitude;
        @SerializedName("Kedalaman") public String kedalaman;
        @SerializedName("Wilayah") public String wilayah;
        @SerializedName("Potensi") public String potensi;
    }
}