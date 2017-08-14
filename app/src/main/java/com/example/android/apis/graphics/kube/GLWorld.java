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
    private ArrayList<GLVertex> mVertexList = new ArrayList<>();

    private int mIndexCount = 0;

    private IntBuffer mVertexBuffer;
    private IntBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;

    public void addShape(GLShape shape) {
        mShapeList.add(shape);
        mIndexCount += shape.getIndexCount();
    }

    public GLVertex addVertex(float x, float y, float z) {
        GLVertex vertex = new GLVertex(x, y, z, mVertexList.size());
        mVertexList.add(vertex);
        return vertex;
    }

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

    public void transformVertex(GLVertex vertex, M4 transform) {
        vertex.update(mVertexBuffer, transform);
    }

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

    @SuppressWarnings("unused")
    static public float toFloat(int x) {
        return x / 65536.0f;
    }
}
