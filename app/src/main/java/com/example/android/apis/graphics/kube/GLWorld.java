/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics.kube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Handy class which contains lists of {@code GLShape} and {@code GLVertex} objects which can be
 * added to using the methods {@code addShape} and {@code addVertex} respectively. When done adding
 * these objects, you can call the method {@code generate} which translates these objects into the
 * format required to feed to the openGL method {@code glDrawElements} (the vertex buffer
 * {@code IntBuffer mVertexBuffer}, the color buffer {@code IntBuffer mColorBuffer} and the index
 * buffer {@code ShortBuffer mIndexBuffer}. It also contains the method {@code transformVertex} which
 * will apply an {@code M4} transformation matrix to a vertex so that the rubic cube can be animated.
 * The {@code draw} method is called from the {@code onDrawFrame} callback of {@code KubeRenderer}
 * whenever it is necessary to draw the latest version of our rubic cube ({@code generate} has to have
 * been called first of course).
 */
@SuppressWarnings("WeakerAccess")
public class GLWorld {
    /**
     * Count of number of times our {@code draw} method has been called (for debugging purposes?)
     */
    int count = 0;

    /**
     * List of all the {@code GLShape} objects comprising our rubic cube.
     */
    private ArrayList<GLShape> mShapeList = new ArrayList<>();
    /**
     * List of all the {@code GLVertex} objects comprising our rubic cube.
     */
    private ArrayList<GLVertex> mVertexList = new ArrayList<>();

    /**
     * Count of number of indices required to divide our Rubic cube into GL_TRIANGLES, it is used to
     * allocate space for {@code ShortBuffer mIndexBuffer} and as the {@code count} argument to the
     * call of {@code glDrawElements}.
     */
    private int mIndexCount = 0;

    /**
     * Direct allocated buffer used as the vertex buffer in the call of {@code glDrawElements}, once
     * the entire Rubic cube has been generated, each {@code GLVertex} in our list {@code mVertexList}
     * is instructed to add its x,y,z coordinates to {@code mVertexBuffer} (in the same call to
     * {@code GLVertex.put} which adds its color to {@code IntBuffer mColorBuffer}.
     */
    private IntBuffer mVertexBuffer;
    /**
     * Direct allocated buffer used as the color buffer in the call of {@code glDrawElements}, once
     * the entire Rubic cube has been generated, each {@code GLVertex} in our list {@code mVertexList}
     * is instructed to add its color to {@code IntBuffer mColorBuffer} (in the same call to
     * {@code GLVertex.put} which adds its x,y,z coordinates to {@code mVertexBuffer}).
     */
    private IntBuffer mColorBuffer;
    /**
     * Direct allocated buffer used as the index buffer in the call of {@code glDrawElements}, it is
     * constructed in our method {@code generate} by requesting each {@code GLShape} in our list
     * {@code ArrayList<GLShape> mShapeList} to add its indices to {@code mIndexBuffer} using its
     * method {@code putIndices}.
     */
    private ShortBuffer mIndexBuffer;

    /**
     * Adds its parameter {@code GLShape shape} to our list {@code ArrayList<GLShape> mShapeList} and
     * updates {@code mIndexCount} by adding the number of indices required by the {@code GLShape} to
     * it.
     *
     * @param shape {@code GLShape} to add to {@code ArrayList<GLShape> mShapeList}
     */
    public void addShape(GLShape shape) {
        mShapeList.add(shape);
        mIndexCount += shape.getIndexCount();
    }

    /**
     * Constructs a {@code GLVertex vertex} from its parameters, adds it to our list of vertices
     * {@code ArrayList<GLVertex> mVertexList}, and returns it to the caller.
     *
     * @param x x coordinate of new vertex
     * @param y y coordinate of new vertex
     * @param z z coordinate of new vertex
     * @return the {@code GLVertex} object we have constructed and added to our list of vertices
     * {@code ArrayList<GLVertex> mVertexList}
     */
    public GLVertex addVertex(float x, float y, float z) {
        GLVertex vertex = new GLVertex(x, y, z, mVertexList.size());
        mVertexList.add(vertex);
        return vertex;
    }

    /**
     * Allocates and fills the direct allocated buffers required by the method {@code glDrawElements}
     * when it draws our Rubic cube: {@code IntBuffer mColorBuffer}, {@code IntBuffer mVertexBuffer}
     * and {@code ShortBuffer mIndexBuffer}. First we direct allocate {@code ByteBuffer bb} to be
     * the correct number of bytes required for a color buffer (4 bytes per component, and 4 components
     * for each vertex in {@code ArrayList<GLVertex> mVertexList}), set its byte order to native order
     * and initialize {@code IntBuffer mColorBuffer} with a view of {@code bb} as an int buffer.
     * Then we direct allocate {@code bb} to have the correct number of bytes required for a vertex
     * buffer (4 bytes per coordinate, and 3 coordinates per vertex), set its byte order to native
     * order and initialize {@code IntBuffer mVertexBuffer} with a view of {@code bb} as an int buffer.
     * Next we direct allocate {@code bb} to have the correct number of bytes required for an index
     * buffer (2 bytes per index value), set its byte order to native order and initialize
     * {@code ShortBuffer mIndexBuffer} with a view of {@code bb} as an short buffer.
     * <p>
     * Now we iterate through all the {@code GLVertex} objects in {@code ArrayList<GLVertex> mVertexList}
     * fetching the next {@code GLVertex vertex} and calling its {@code put} method to add its
     * coordinates to {@code IntBuffer mVertexBuffer} and its color to {@code IntBuffer mColorBuffer}.
     * <p>
     * Finally we iterate through all the {@code GLShape} objects in {@code ArrayList<GLShape> mShapeList}
     * fetching the next {@code GLShape shape} and calling its {@code putIndices} method to add the
     * index values to {@code ShortBuffer mIndexBuffer} which will divide the {@code GLShape} into
     * GL_TRIANGLES for {@code glDrawElements} to draw.
     */
    public void generate() {
        ByteBuffer bb = ByteBuffer.allocateDirect(mVertexList.size() * 4 * 4);
        bb.order(ByteOrder.nativeOrder());
        mColorBuffer = bb.asIntBuffer();

        bb = ByteBuffer.allocateDirect(mVertexList.size() * 4 * 3);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asIntBuffer();

        bb = ByteBuffer.allocateDirect(mIndexCount * 2);
        bb.order(ByteOrder.nativeOrder());
        mIndexBuffer = bb.asShortBuffer();

        Iterator<GLVertex> iter2 = mVertexList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter2.hasNext()) {
            GLVertex vertex = iter2.next();
            vertex.put(mVertexBuffer, mColorBuffer);
        }

        Iterator<GLShape> iter3 = mShapeList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter3.hasNext()) {
            GLShape shape = iter3.next();
            shape.putIndices(mIndexBuffer);
        }
    }

    /**
     * Applies a {@code M4 transform} to the coordinates of a {@code GLVertex vertex} and stores the
     * result in our direct allocated vertex buffer {@code IntBuffer mVertexBuffer} ready for the next
     * call to {@code draw} (the original coordinates of {@code GLVertex vertex} remain unchanged).
     * Called from {@code GLShape.animateTransform} for each {@code GLVertex} in the {@code GLShape},
     * it is called from {@code Layer.setAngle}, which is called from {@code Kube.animate}, which is
     * called from {@code KubeRenderer.onDrawFrame}. We simply call the method {@code vertex.update}
     * which multiplies its coordinates by the {@code transform} transform matrix and places the new
     * values in the correct position in our field {@code IntBuffer mVertexBuffer}.
     *
     * @param vertex    {@code GLVertex} to apply the {@code M4 transform} to
     * @param transform transform matrix that moves vertex to new position.
     */
    public void transformVertex(GLVertex vertex, M4 transform) {
        vertex.update(mVertexBuffer, transform);
    }

    /**
     * Called from {@code KubeRenderer.onDrawFrame} to draw our Rubic cube. First we reset the position
     * of the direct allocated buffers used by {@code glDrawElements}: {@code IntBuffer mColorBuffer}
     * (color buffer), {@code IntBuffer mVertexBuffer} (vertex buffer), and {@code ShortBuffer mIndexBuffer}
     * (index buffer). Then we specify the orientation of front-facing polygons to be GL_CW, and select
     * flat shade model.
     * <p>
     * We define an array of vertex data to have 3 coordinates per vertex, GL_FIXED as its data type,
     * 0 for its stride, and {@code IntBuffer mVertexBuffer} as the pointer to the first element in
     * the array.
     * <p>
     * We define an array of colors to have 4 color components per color, GL_FIXED as its data type,
     * 0 for its stride, and {@code IntBuffer mColorBuffer} as a pointer to the first component of
     * the first color element in the array.
     * <p>
     * Finally we call {@code glDrawElements} to render primitives from the array data we specified
     * above, using GL_TRIANGLES as the primitives to draw, {@code mIndexCount} the number of elements
     * to use from the arrays, and {@code ShortBuffer mIndexBuffer} as the pointer to the location
     * where the indices are stored.
     * <p>
     * We increment {@code count} for no apparent reason.
     *
     * @param gl the GL interface.
     */
    public void draw(GL10 gl) {
        mColorBuffer.position(0);
        mVertexBuffer.position(0);
        mIndexBuffer.position(0);

        gl.glFrontFace(GL10.GL_CW);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        count++;
    }

    /**
     * Unused so who cares.
     *
     * @param x who cares
     * @return who cares
     */
    @SuppressWarnings("unused")
    static public float toFloat(int x) {
        return x / 65536.0f;
    }
}
