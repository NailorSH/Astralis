//package com.nailorsh.astralis
//
//import com.danielgergely.kgl.KglAndroid
//import com.danielgergely.kgl.Shader
//import com.nailorsh.astralis.core.kgl.setUniformValue
//import com.nailorsh.astralis.core.utils.graphics.math.matrix.Mat4d
//import com.nailorsh.astralis.core.utils.graphics.math.matrix.Mat4f
//import com.nailorsh.astralis.core.utils.graphics.math.toMat4f
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vec3d
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vec3f
//import com.nailorsh.astralis.projector.invert
//
//abstract class AstralisProjector(private val modelViewTransform: ModelViewTranform) {
//    fun project(vector: Vec3d, win: Vec3f): Boolean {
//        val wind = vector
//        val res: Boolean = projectInPlace(wind)
//        win.set(wind.toVec3f())
//        return res
//    }
//
//    fun projectInPlace(vector: Vec3d): Boolean {
//        modelViewTransform.forward(vector);
//        val floatVector = vector.toVec3f();
////        val rval = forward(vector);
////        // very important: even when the projected point comes from an
////        // invisible region of the sky (rval=false), we must finish
////        // reprojecting, so that OpenGL can successfully eliminate
////        // polygons by culling.
////        vd[0] = viewportCenter[0] + static_cast<double>(flipHorz * pixelPerRad * v[0]);
////        vd[1] = viewportCenter[1] + static_cast<double>(flipVert * pixelPerRad * v[1]);
////        vd[2] = (static_cast<double>(v[2]) - zNear) * oneOverZNearMinusZFar;
////        return rval;
//    }
//
//    abstract class ModelViewTranform {
//        abstract fun forward(vector: Vec3d)
//        abstract fun backward(vector: Vec3d)
//        abstract fun forward(vector: Vec3f)
//        abstract fun backward(vector: Vec3f)
//
//        abstract fun combine(matrix: Mat4d)
//        abstract fun clone(): ModelViewTranform
//
//        abstract fun getApproximateLinearTransfo(): Mat4d
//
//        abstract fun getForwardTransformShader(): ByteArray
//        abstract fun setForwardTransformUniforms(program: Shader)
//        abstract fun getBackwardTransformShader(): ByteArray
//        abstract fun setBackwardTransformUniforms(program: Shader)
//    }
//
//    class Mat4dTransform(
//        altAzToWorld: Mat4d,
//        vertexToAltAzPos: Mat4d
//    ) : ModelViewTranform() {
//        // transfo matrix and invert
//        private var transfoMat: Mat4d = altAzToWorld * vertexToAltAzPos
//        private var transfoMatf: Mat4f = transfoMat.toMat4f()
//
//        // Transforms a vertex from model space to coordinates where
//        // Z is zenith, so zenith angle can easily be computed.
//        private var vertexToAltAzPos: Mat4f = vertexToAltAzPos.toMat4f()
//
//        // Transforms view direction from the projector's frame to coordinates where
//        // Z is zenith, so zenith angle can easily be computed.
//        private var worldPosToAltAzPos: Mat4f = altAzToWorld.inverse().toMat4f()
//
//        override fun forward(vector: Vec3d) = vector.transfo4d(transfoMat)
//        override fun forward(vector: Vec3f) = vector.transfo4d(transfoMatf)
//
//        override fun backward(vector: Vec3d) {
//            // We need no matrix inversion because we always work with orthogonal matrices
//            // (where the transposed is the inverse).
//            val x = vector.x - transfoMat.array[12]
//            val y = vector.y - transfoMat.array[13]
//            val z = vector.z - transfoMat.array[14]
//
//            vector.x = transfoMat.array[0] * x + transfoMat.array[1] * y + transfoMat.array[2] * z
//            vector.y = transfoMat.array[4] * x + transfoMat.array[5] * y + transfoMat.array[6] * z
//            vector.z = transfoMat.array[8] * x + transfoMat.array[9] * y + transfoMat.array[10] * z
//        }
//
//        override fun backward(vector: Vec3f) {
//            // We need no matrix inversion because we always work with orthogonal matrices
//            // (where the transposed is the inverse).
//            val x = vector.x - transfoMatf.array[12]
//            val y = vector.y - transfoMatf.array[13]
//            val z = vector.z - transfoMatf.array[14]
//
//            vector.x =
//                (transfoMatf.array[0] * x + transfoMatf.array[1] * y + transfoMatf.array[2] * z).toFloat()
//            vector.y =
//                (transfoMatf.array[4] * x + transfoMatf.array[5] * y + transfoMatf.array[6] * z).toFloat()
//            vector.z =
//                (transfoMatf.array[8] * x + transfoMatf.array[9] * y + transfoMatf.array[10] * z).toFloat()
//        }
//
//        override fun combine(matrix: Mat4d) {
//            transfoMat *= matrix;
//            transfoMatf = transfoMat.toMat4f()
//        }
//
//        override fun clone(): ModelViewTranform = this
//
//        override fun getApproximateLinearTransfo(): Mat4d = transfoMat
//
//        override fun getForwardTransformShader(): ByteArray {
//            return """
//            uniform mat4 PROJECTOR_vertexToAltAzPosMatrix;
//            uniform mat4 PROJECTOR_modelViewMatrix;
//            vec3 modelViewForwardTransform(vec3 v)
//            {
//            	return (PROJECTOR_modelViewMatrix * vec4(v,1)).xyz;
//            }
//            vec3 vertexToAltAzPos(vec3 v)
//            {
//            	return (PROJECTOR_vertexToAltAzPosMatrix * vec4(v,1)).xyz;
//            }
//        """.trimIndent().toByteArray()
//        }
//
//        override fun setForwardTransformUniforms(program: Shader) {
//            KglAndroid.setUniformValue(
//                program,
//                "PROJECTOR_modelViewMatrix",
//                transfoMatf.toFloatArray()
//            )
//            KglAndroid.setUniformValue(
//                program,
//                "PROJECTOR_vertexToAltAzPosMatrix",
//                vertexToAltAzPos.toFloatArray()
//            )
//        }
//
//        override fun getBackwardTransformShader(): ByteArray {
//            return """
//            uniform mat4 PROJECTOR_modelViewMatrixInverse;
//            uniform mat4 PROJECTOR_worldPosToAltAzPosMatrix;
//            vec3 modelViewBackwardTransform(vec3 worldPos)
//            {
//            	return (PROJECTOR_modelViewMatrixInverse * vec4(worldPos,1)).xyz;
//            }
//            vec3 worldPosToAltAzPos(vec3 worldPos)
//            {
//            	return (PROJECTOR_worldPosToAltAzPosMatrix * vec4(worldPos,1)).xyz;
//            }
//        """.trimIndent().toByteArray()
//        }
//
//        override fun setBackwardTransformUniforms(program: Shader) {
//            KglAndroid.setUniformValue(
//                program,
//                "PROJECTOR_modelViewMatrixInverse",
//                transfoMatf.toFloatArray().invert()
//            )
//            KglAndroid.setUniformValue(
//                program,
//                "PROJECTOR_worldPosToAltAzPosMatrix",
//                worldPosToAltAzPos.toFloatArray()
//            )
//        }
//    }
//}