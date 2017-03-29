# Voxcom

Voxcom is a tool for compositing multiple [MagicaVoxel](https://ephtracy.github.io/) models into a combined scene.
You can specify in a `.yaml` file the location and orientation of each `.vox` model.
The output is a `.vox` file with the composited scene.
If the models have different palettes, Voxcom intelligently merges them into a single, unified palette in the final output.
It greatly reduces the manual work of combining reusable models into scenes.
We wrote it to speed up our modeling for our mobile game [Road Trip](http://larvalabs.com/roadtrip).

## Example

Here are three Magica models:

![Road](https://github.com/larvalabs/voxcom/blob/master/images/road.png)

![Tree](https://github.com/larvalabs/voxcom/blob/master/images/tree.png)

![Car](https://github.com/larvalabs/voxcom/blob/master/images/car.png)

This `.yaml` file specifies how the scene should be composited:

```yaml
output: "road_scene.vox"
ignoreMaterials: no
models:
 - name: "vox/road.vox"
 - name: "vox/tree.vox"
   posX: 10
   posY: 20.5
   posZ: 2
   centerX: yes
   centerY: yes
   rotateZ: 270
 - name: "vox/tree.vox"
   posX: 9
   posY: 60.5
   posZ: 2
   centerX: yes
   centerY: yes
   rotateZ: 180
 - name: "vox/tree.vox"
   posX: 10
   posY: 100.5
   posZ: 2
   centerX: yes
   centerY: yes
   rotateZ: 90
 - name: "vox/tree.vox"
   posX: 111
   posY: 20.5
   posZ: 2
   centerX: yes
   centerY: yes
   rotateZ: 90
 - name: "vox/tree.vox"
   posX: 112
   posY: 60.5
   posZ: 2
   centerX: yes
   centerY: yes
   rotateZ: 180
 - name: "vox/tree.vox"
   posX: 111
   posY: 100.5
   posZ: 2
   centerX: yes
   centerY: yes
   rotateZ: 90
 - name: "vox/car_blue.vox"
   posX: 40.5
   posY: 75
   posZ: 1
   centerX: yes
   centerY: yes
   rotateZ: 0
 - name: "vox/car_blue.vox"
   posX: 67.5
   posY: 50
   posZ: 1
   centerX: yes
   centerY: yes
   rotateZ: 180
```

And here is the composited result from Voxcom:

![Composited Scene](https://github.com/larvalabs/voxcom/blob/master/images/roadScene.png "Composited Scene")

## Usage

To use Voxcom, you must have [Java](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) installed.
In a terminal window, change into the voxcom directory and run:
```
java -jar voxcom.jar <scene.yaml>
```
You can use `road.yaml` that is provided in the distribution to test it out.
When you are ready to make your own scene, here is the format of the `.yaml` file:

1. At the top level, you can specify the output file with `output: filename.vox`. If omitted, the default is `scene.vox`.
2. Also at the top level, you can indicate if you wish materials to be ignored when merging the model palettes with `ignoreMaterials: yes`. The default if omitted is `no`. This is discussed in more detail later.
3. Next comes the list of models, beginning with a `models:` line.
    1. The `name` field is the path to the `.vox` file.
    2. The position is specified with `posX`, `posY`, and `posZ`. They default to `0` if missing.
    3. The model is positioned so that its origin `(0, 0, 0)` is placed at `(posX, posY, posZ)`.
If you would like to center the model along any of the axes instead, then specify `centerX: yes`, `centerY: yes`, and/or `centerZ: yes`.
Note that if you wish to center a model along an axis with an even number of voxels, you can specify the position at the half way point between two voxels to make it clear where you would like it to be centered.
This is done in the example above.
    4. You can flip the model with respect to its axes with `flipX: yes`, `flipY: yes`, and `flipZ: yes`.
    5. You can rotate the model `90`, `180` or `270` degrees with `rotateX: N`, `rotateY: N`, and `rotateZ: N`.
The rotations are processed first for the X axis, then the Y, then finally the Z, and these are all processed after the flips above.

## Palette Merging

MagicaVoxel only supports 255 palette entries. Each entry has a color, as well as various material settings (glass, emission, metal).
If models are merged naively, this information can be lost.
Voxcom keeps track of which palette entries are actually being used by the model, and preserves those.
As new models are composited, it will reuse existing palette entries if they exactly match the incoming entries.
For those that don't match, an unused palette entry is overwriten with the new entry.
If this process results in more than 255 unique palette entries, then undefined behavior will result.
if there is demand for it, an algorithm to produce the best compromise palette could be attempted.
If the `ignoreMaterials` flag is switched on, then all material settings are disregarded and only the color is used to merge palettes.