package com.larvalabs.voxcom;

import java.io.*;
import java.util.ArrayList;

/**
 * Reads and writes the .vox format.
 *
 * @author John Watkinson
 */
public class VoxFormat {

    private static final String TAG_FORMAT = "VOX ";
    private static final String TAG_MAIN = "MAIN";
    private static final String TAG_PACK = "PACK";
    private static final String TAG_SIZE = "SIZE";
    private static final String TAG_XYZI = "XYZI";
    private static final String TAG_RGBA = "RGBA";
    private static final String TAG_MATT = "MATT";

    private static final int VERSION = 150;

    public static void write(VoxModel model, OutputStream outputStream) throws IOException {
        DataOutputStream out = new DataOutputStream(outputStream);

        // Format Tag
        writeRiffTag(TAG_FORMAT, out);

        // Format version
        writeInt(VERSION, out);

        // Main Chunk
        writeRiffTag(TAG_MAIN, out);

        {
            ByteArrayOutputStream mainBytes = new ByteArrayOutputStream();
            DataOutputStream mainOut = new DataOutputStream(mainBytes);

            // Size Chunk
            writeRiffTag(TAG_SIZE, mainOut);
            // The size of this chunk
            writeInt(12, mainOut);
            writeInt(0, mainOut);
            writeInt(model.getSizeX(), mainOut);
            writeInt(model.getSizeY(), mainOut);
            writeInt(model.getSizeZ(), mainOut);

            // XYZI Chunk
            writeRiffTag(TAG_XYZI, mainOut);
            ArrayList<Voxel> voxels = model.getVoxels();
            int voxelChunkSize = 4 + voxels.size() * 4;
            writeInt(voxelChunkSize, mainOut);
            writeInt(0, mainOut);
            writeInt(voxels.size(), mainOut);
            for (Voxel voxel : voxels) {
                writeIntoBytes(mainOut, voxel.x, voxel.y, voxel.z, voxel.i);
            }

            // RGBA Chunk
            writeRiffTag(TAG_RGBA, mainOut);
            writeInt(1024, mainOut);
            writeInt(0, mainOut);
            Palette palette = model.getPalette();
            for (int i = 1; i < 256; i++) {
                mainOut.writeInt(palette.getColor(i));
            }
            mainOut.writeInt(0);

            // MATT Chunks
            for (int i = 1; i < 256; i++) {
                Material material = model.getMaterial(i);
                if (material.type != 0) {
                    writeRiffTag(TAG_MATT, mainOut);
                    int size = 4 * (4 + material.values.length);
                    writeInt(size, mainOut);
                    writeInt(0, mainOut);
                    writeInt(i, mainOut);
                    writeInt(material.type, mainOut);
                    writeFloat(material.weight, mainOut);
                    writeInt(material.properties, mainOut);
                    for (int j = 0; j < material.values.length; j++) {
                        writeFloat(material.values[j], mainOut);
                    }
                }
            }

            mainOut.flush();
            byte[] main = mainBytes.toByteArray();
            writeInt(0, out);
            writeInt(main.length, out);
            out.write(main);
        }
    }

    public static VoxModel read(InputStream inputStream) throws IOException {
        DataInputStream in = new DataInputStream(inputStream);

        // Format Tag
        String formatTag = readRiffTag(in);
        if (!formatTag.equals(TAG_FORMAT)) {
            throw new IOException("Doesn't appear to be in VOX format.");
        }

        // Format Version
        int version = readInt(in);
        if (version != VERSION) {
            System.out.println("Warning: expecting version " + VERSION + " but got " + version + ".");
        }

        // Main Chunk
        String chunkTag = readRiffTag(in);
        if (!chunkTag.equals(TAG_MAIN)) {
            throw new IOException("Should be a " + TAG_MAIN + " tag here.");
        }
        skip(in, 8);

        // Optional pack chunk
        int numModels = 1;
        String tag = readRiffTag(in);
        if (tag.equals(TAG_PACK)) {
            skip(in, 8);
            numModels = readInt(in);
            tag = readRiffTag(in);
        }

        // todo, eventually deal with more than one model, for now just load the first
        // Size chunk
        if (!tag.equals(TAG_SIZE)) {
            throw new IOException("Should be a " + TAG_SIZE + " tag here.");
        }
        skip(in, 8);
        int sizeX = readInt(in);
        int sizeY = readInt(in);
        int sizeZ = readInt(in);

        // Voxel data chunk
        String xyziTag = readRiffTag(in);
        if (!xyziTag.equals(TAG_XYZI)) {
            throw new IOException("Should be a " + TAG_XYZI + " tag here.");
        }
        skip(in, 8);
        int numVoxels = readInt(in);
        VoxModel model = new VoxModel(sizeX, sizeY, sizeZ);
        byte[] voxel = new byte[4];
        for (int i = 0; i < numVoxels; i++) {
            readIntoBytes(in, voxel);
            int x = Byte.toUnsignedInt(voxel[0]);
            int y = Byte.toUnsignedInt(voxel[1]);
            int z = Byte.toUnsignedInt(voxel[2]);
            int p = Byte.toUnsignedInt(voxel[3]);
            model.addVoxel(x, y, z, p);
            // System.out.println("Voxel x=" + x + ", y=" + y + ", z=" + z + ", p=" + p);
        }

        // Palette/Material chunk
        while (true) {
            tag = null;
            try {
                tag = readRiffTag(in);
            } catch (EOFException eof) {
                // No palette or material data, just skip this section.
            }
            if (tag != null) {
                if (TAG_RGBA.equals(tag)) {
                    skip(in, 8);
                    for (int i = 0; i < 255; i++) {
                        int c = in.readInt();
                        model.setColor(i + 1, c);
                    }
                } else if (TAG_MATT.equals(tag)) {
                    int numValues = readInt(in)/4 - 4;
                    skip(in, 4);
                    int index = readInt(in);
                    Material material = model.getMaterial(index);
                    material.type = readInt(in);
                    material.weight = readFloat(in);
                    material.properties = readInt(in);
                    material.values = new float[numValues];
                    for (int i = 0; i < numValues; i++) {
                        material.values[i] = readFloat(in);
                    }
                }
            } else {
                break;
            }
        }
        return model;
    }

    private static String readRiffTag(DataInputStream in) throws IOException {
        char[] c = new char[4];
        for (int i = 0; i < 4; i++) {
            c[i] = (char) in.readByte();
        }
        return new String(c);
    }

    private static void writeRiffTag(String tag, DataOutputStream out) throws IOException {
        byte[] bytes = tag.getBytes();
        out.write(bytes);
    }

    private static int readInt(DataInputStream in) throws IOException {
        int i = in.readInt();
        return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
    }

    private static void writeInt(int i, DataOutputStream out) throws IOException {
        out.writeInt((i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff);
    }

    private static float readFloat(DataInputStream in) throws IOException {
        int i = in.readInt();
        return Float.intBitsToFloat((i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff);
    }

    private static void writeFloat(float v, DataOutputStream out) throws IOException {
        int i = Float.floatToIntBits(v);
        out.writeInt((i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff);
    }

    private static void readIntoBytes(DataInputStream in, byte[] bytes) throws IOException {
        in.read(bytes);
    }

    private static void writeIntoBytes(DataOutputStream out, int a, int b, int c, int d) throws IOException {
        byte[] bytes = new byte[4];
        int[] v = {a, b, c, d};
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte)(v[i] & 0xFF);
        }
        out.write(bytes);
    }

    private static void skip(DataInputStream in, int numBytes) throws IOException {
        for (int i = 0; i < numBytes; i++) {
            in.readByte();
        }
    }

    public static void main(String[] args) throws Exception {
        FileInputStream in = new FileInputStream("vox/test_materials.vox");
        VoxModel model = read(in);
        FileOutputStream out = new FileOutputStream("vox/test_roundtrip.vox");
        write(model, out);
        out.close();
    }

}
