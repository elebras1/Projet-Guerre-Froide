package com.populaire.projetguerrefroide.adapter.graphics;

import com.badlogic.gdx.math.Matrix4;

public class WgProjection {

    private final Matrix4 projectionMatrix;

    private final Matrix4 transformMatrix;

    private final Matrix4 shiftDepthMatrix;

    private final Matrix4 combinedMatrix;

    public WgProjection() {
        this.projectionMatrix = new Matrix4();
        this.transformMatrix = new Matrix4();
        // matrix which will transform an opengl ortho matrix to a webgpu ortho matrix
        // by scaling the Z range from [-1..1] to [0..1]
        this.shiftDepthMatrix = new Matrix4().idt().scl(1,1,0.5f).trn(0,0,0.5f);
        this.combinedMatrix = new Matrix4();
    }

    public void setProjectionMatrix(Matrix4 matrix) {
        this.projectionMatrix.set(matrix);
        this.updateMatrices();
    }

    private void updateMatrices(){
        this.combinedMatrix.set(this.shiftDepthMatrix).mul(this.projectionMatrix).mul(this.transformMatrix);
    }

    public Matrix4 getCombinedMatrix() {
        return this.combinedMatrix;
    }
}
