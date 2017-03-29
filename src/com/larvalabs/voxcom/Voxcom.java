package com.larvalabs.voxcom;

import org.ho.yaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main entry point for Voxcom. Parses the provided YAML file, composites the scene and writes out the resulting .vox
 * file.
 *
 * @author John Watkinson
 */
public class Voxcom {

    public static boolean ignoreMaterials = false;

    public static void main(String[] args) throws Exception{
        System.out.println("Welcome to Voxcom \uD83D\uDC7E");
        if (args.length == 0) {
            showUsage();
        } else {
            parseYaml(args[0]);
        }
    }

    private static void showUsage() {
        System.out.println("Usage: java -jar voxcom.jar <scene.yaml>");
    }

    private static void parseYaml(String file) throws Exception {
        String outFile = "scene.vox";
        HashMap yaml = (HashMap) Yaml.load(new File(file));
        if (yaml.containsKey("output")) {
            outFile = yaml.get("output").toString();
        }
        // If set, ignore materials when merging palettes, just use color.
        ignoreMaterials = getBoolean(yaml, "ignoreMaterials", false);
        ArrayList<HashMap> models = (ArrayList) yaml.get("models");
        VoxModel parent = new VoxModel(1, 1, 1);
        for (HashMap model : models) {
            String filename = model.get("name").toString();
            float posX = getFloat(model, "posX", 0);
            float posY = getFloat(model, "posY", 0);
            float posZ = getFloat(model, "posZ", 0);
            boolean centerX = getBoolean(model, "centerX", false);
            boolean centerY = getBoolean(model, "centerY", false);
            boolean centerZ = getBoolean(model, "centerZ", false);
            boolean flipX = getBoolean(model, "flipX", false);
            boolean flipY = getBoolean(model, "flipY", false);
            boolean flipZ = getBoolean(model, "flipZ", false);
            int rotateX = getInt(model, "rotateX", 0);
            int rotateY = getInt(model, "rotateY", 0);
            int rotateZ = getInt(model, "rotateZ", 0);
            System.out.println(" - Adding '" + filename + "'...");
            FileInputStream modelIn = new FileInputStream(filename);
            VoxModel vm = VoxFormat.read(modelIn);
            modelIn.close();
            parent.add(vm, posX, posY, posZ, centerX, centerY, centerZ, flipX, flipY, flipZ, rotateX, rotateY, rotateZ);
        }
        parent.clipToVoxels();
        System.out.println(" - Writing result to '" + outFile + "'...");
        FileOutputStream out = new FileOutputStream(outFile);
        VoxFormat.write(parent, out);
        out.close();
        System.out.println("Done.");
    }

    private static int getInt(HashMap map, String key, int defaultValue) {
        Object obj = map.get(key);
        if (obj == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(obj.toString());
        }
    }

    private static float getFloat(HashMap map, String key, float defaultValue) {
        Object obj = map.get(key);
        if (obj == null) {
            return defaultValue;
        } else {
            return Float.parseFloat(obj.toString());
        }
    }

    private static boolean getBoolean(HashMap map, String key, boolean defaultValue) {
        Object obj = map.get(key);
        if (obj == null) {
            return defaultValue;
        } else {
            String s = obj.toString().toLowerCase();
            if ("yes".equals(s)) {
                return true;
            } else if ("no".equals(s)) {
                return false;
            } else {
                return Boolean.parseBoolean(obj.toString());
            }
        }
    }

}
