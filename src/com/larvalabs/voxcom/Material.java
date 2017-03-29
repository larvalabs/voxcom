package com.larvalabs.voxcom;

import java.util.Arrays;

/**
 * Stores the material information for a palette entry.
 *
 * @author John Watkinson
 */
public class Material {

    public int index;
    public boolean used;

    public int color;

    public int type;
    public float weight;

    public int properties;

    public float[] values;

    public Material() {
        this.color = 0xFF000000;
        used = false;
        type = 0;
        weight = 1;
        properties = 0;
        values = new float[0];
    }

    public void copyFrom(Material other) {
        color = other.color;
        if (!Voxcom.ignoreMaterials) {
            type = other.type;
            weight = other.weight;
            properties = other.properties;
            values = other.values;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Material material = (Material) o;

        if (color != material.color) return false;

        // Forget the rest if we are ignoring materials and the colors match
        if (Voxcom.ignoreMaterials) return true;

        if (type != material.type) return false;
        if (Float.compare(material.weight, weight) != 0) return false;
        if (properties != material.properties) return false;
        return Arrays.equals(values, material.values);

    }

    @Override
    public int hashCode() {
        int result = color;
        if (!Voxcom.ignoreMaterials) {
            result = 31 * result + type;
            result = 31 * result + (weight != +0.0f ? Float.floatToIntBits(weight) : 0);
            result = 31 * result + properties;
            result = 31 * result + Arrays.hashCode(values);
        }
        return result;
    }

}
