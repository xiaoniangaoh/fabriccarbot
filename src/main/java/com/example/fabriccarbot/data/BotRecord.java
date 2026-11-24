package com.example.fabriccarbot.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Locale;

public class BotRecord {
    @SerializedName("name")
    public String name;

    @SerializedName("aliases")
    public List<String> aliases;

    @SerializedName("dimension")
    public String dimension;

    @SerializedName("x")
    public double x;
    @SerializedName("y")
    public double y;
    @SerializedName("z")
    public double z;

    @SerializedName("yaw")
    public float yaw;
    @SerializedName("pitch")
    public float pitch;

    public BotRecord() {}

    public BotRecord(String name, List<String> aliases, String dimension,
                     double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.aliases = aliases;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getFirstAliasOrName() {
        if (aliases != null && !aliases.isEmpty()) return aliases.get(0);
        return name;
    }
}