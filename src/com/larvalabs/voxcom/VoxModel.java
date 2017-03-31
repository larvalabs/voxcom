package com.larvalabs.voxcom;

import java.util.ArrayList;

/**
 * Represents a .vox voxel model.
 *
 * @author John Watkinson
 */
public class VoxModel {

    private static final int MAX_SIZE = 126;

    private int sizeX, sizeY, sizeZ;

    private Palette palette = new Palette();

    private ArrayList<Voxel> voxels = new ArrayList<>();

    public VoxModel(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public void setColor(int index, int color) {
        palette.setColor(index, color);
    }

    public Material getMaterial(int index) {
        return palette.getMaterial(index);
    }

    public void addVoxel(int x, int y, int z, int i) {
        if (x >= 0 && x < MAX_SIZE && y >= 0 && y < MAX_SIZE && z >= 0 && z < MAX_SIZE) {
            voxels.add(new Voxel(x, y, z, i));
        }
        palette.setUsed(i);
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public Palette getPalette() {
        return palette;
    }

    public ArrayList<Voxel> getVoxels() {
        return voxels;
    }

    public void add(VoxModel model, int x, int y, int z) {
        add(model, x, y, z, false, false, false, false, false, false, 0, 0, 0);
    }

    public void scale(int f) {
        if (f == 1) {
            return;
        } else {
            ArrayList<Voxel> oldVoxels = voxels;
            voxels = new ArrayList<>();
            for (Voxel voxel : oldVoxels) {
                int x = voxel.x * f;
                int y = voxel.y * f;
                int z = voxel.z * f;
                for (int dx = 0; dx < f; dx++) {
                    for (int dy = 0; dy < f; dy++) {
                        for (int dz = 0; dz < f; dz++) {
                            addVoxel(x + dx, y + dy, z + dz, voxel.i);
                        }
                    }
                }
            }
            sizeX = Math.min(sizeX * f, MAX_SIZE);
            sizeY = Math.min(sizeY * f, MAX_SIZE);
            sizeZ = Math.min(sizeZ * f, MAX_SIZE);
        }
    }

    public void add(VoxModel model, float x, float y, float z, boolean centerX, boolean centerY, boolean centerZ, boolean flipX, boolean flipY, boolean flipZ, int rotateX, int rotateY, int rotateZ) {
        palette.merge(model);
        float[][] rot = new float[3][3];
        rot[0][0] = cos(rotateY) * cos(rotateZ);
        rot[0][1] = cos(rotateZ) * sin(rotateX) * sin(rotateY) - cos(rotateX) * sin(rotateZ);
        rot[0][2] = cos(rotateX) * cos(rotateZ) * sin(rotateY) + sin(rotateX) * sin(rotateZ);
        rot[1][0] = cos(rotateY) * sin(rotateZ);
        rot[1][1] = cos(rotateX) * cos(rotateZ) + sin(rotateX) * sin(rotateY) * sin(rotateZ);
        rot[1][2] = cos(rotateX) * sin(rotateY) * sin(rotateZ) - cos(rotateZ) * sin(rotateX);
        rot[2][0] = -sin(rotateY);
        rot[2][1] = cos(rotateY) * sin(rotateX);
        rot[2][2] = cos(rotateX) * cos(rotateY);
        for (Voxel voxel : model.voxels) {
            int vx = flipX ? model.sizeX - voxel.x - 1 : voxel.x;
            int vy = flipY ? model.sizeY - voxel.y - 1 : voxel.y;
            int vz = flipZ ? model.sizeZ - voxel.z - 1 : voxel.z;
            float fx = vx - model.sizeX/2f;
            float fy = vy - model.sizeY/2f;
            float fz = vz - model.sizeZ/2f;
            int rx = (int) (rot[0][0] * fx + rot[0][1] * fy + rot[0][2] * fz + (centerX ? 1 : model.sizeX / 2f) + x);
            int ry = (int) (rot[1][0] * fx + rot[1][1] * fy + rot[1][2] * fz + (centerY ? 1 : model.sizeY / 2f) + y);
            int rz = (int) (rot[2][0] * fx + rot[2][1] * fy + rot[2][2] * fz + (centerZ ? 1 : model.sizeZ / 2f) + z);
            if (rx >= 0 && rx <= MAX_SIZE && ry >= 0 && ry <= MAX_SIZE && rz >= 0 && rz <= MAX_SIZE) {
                addVoxel(rx, ry, rz, voxel.i);
            }
        }
    }

    public void clipToVoxels() {
        for (Voxel voxel : voxels) {
            sizeX = Math.max(sizeX, voxel.x + 1);
            sizeY = Math.max(sizeY, voxel.y + 1);
            sizeZ = Math.max(sizeZ, voxel.z + 1);
        }
    }

    private static int sin(int angle) {
        switch (angle) {
            case 90:
                return 1;
            case 180:
                return 0;
            case 270:
                return -1;
        }
        return 0;
    }

    private static int cos(int angle) {
        switch (angle) {
            case 90:
                return 0;
            case 180:
                return -1;
            case 270:
                return 0;
        }
        return 1;
    }

}
