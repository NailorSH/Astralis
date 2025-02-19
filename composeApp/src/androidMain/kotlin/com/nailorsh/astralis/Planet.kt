//@file:OptIn(ExperimentalUnsignedTypes::class)
//
//package com.nailorsh.astralis
//
//import android.opengl.GLES20
//import android.opengl.GLES20.GL_TRIANGLES
//import android.opengl.GLES20.GL_UNSIGNED_SHORT
//import astralis.composeapp.generated.resources.Res
//import co.touchlab.kermit.Logger
//import com.danielgergely.kgl.FloatBuffer
//import com.danielgergely.kgl.GL_ARRAY_BUFFER
//import com.danielgergely.kgl.GL_COMPILE_STATUS
//import com.danielgergely.kgl.GL_ELEMENT_ARRAY_BUFFER
//import com.danielgergely.kgl.GL_FLOAT
//import com.danielgergely.kgl.GL_FRAGMENT_SHADER
//import com.danielgergely.kgl.GL_LINK_STATUS
//import com.danielgergely.kgl.GL_STREAM_DRAW
//import com.danielgergely.kgl.GL_VERTEX_SHADER
//import com.danielgergely.kgl.GlBuffer
//import com.danielgergely.kgl.IntBuffer
//import com.danielgergely.kgl.KglAndroid
//import com.danielgergely.kgl.Shader
//import com.danielgergely.kgl.VertexArrayObject
//import com.mayakapps.kache.InMemoryKache
//import com.mayakapps.kache.KacheStrategy
//import com.nailorsh.astralis.core.kgl.bufferSubData
//import com.nailorsh.astralis.core.kgl.enableAttributeArray
//import com.nailorsh.astralis.core.kgl.setAttributeBuffer
//import com.nailorsh.astralis.core.utils.graphic.getApparentSiderealTime
//import com.nailorsh.astralis.core.utils.graphic.getMeanSiderealTime
//import com.nailorsh.astralis.core.utils.graphics.AstralisTexture
//import com.nailorsh.astralis.core.utils.graphics.math.computeCosSinRho
//import com.nailorsh.astralis.core.utils.graphics.math.computeCosSinTheta
//import com.nailorsh.astralis.core.utils.graphics.math.fuzzyEquals
//import com.nailorsh.astralis.core.utils.graphics.math.matrix.Mat4d
//import com.nailorsh.astralis.core.utils.graphics.math.times
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vec3d
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vec3f
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vector3
//import com.nailorsh.astralis.core.utils.graphics.shader.AttributeLocation
//import com.nailorsh.astralis.core.utils.graphics.shader.makeSRGBUtilsShader
//import org.jetbrains.compose.resources.ExperimentalResourceApi
//import kotlin.math.PI
//import kotlin.math.max
//import kotlin.math.sqrt
//
//
//const val J2000 = 2451545.0
//const val ORBIT_SEGMENTS = 360
//
//// The callback type for the external position computation function
//// arguments are JDE, position[3], velocity[3].
//// The last variable is the userData pointer, which is Q_NULLPTR for Planets, but used in derived classes. E.g. points to the KeplerOrbit for Comets.
//
//class Planet3DModel(
//    var vertexArr: FloatArray = floatArrayOf(),
//    var texCoordArr: FloatArray = floatArrayOf(),
//    var indiceArr: UShortArray = ushortArrayOf()
//)
//
//class Planet(
//    private val name: String,
//    private val equatorialRadius: Double,
//    private val oblateness: Double,
//    private val roughness: Float,
//    private val albedo: Float,
//    private val hidden: Boolean,
//    private val eclipticPos: Vec3d,
//    private val aberrationPush: Vec3d,
//    private val rotLocalToParent: Mat4d,
//    private val pType: PlanetType,
//    private val rings: Boolean,
//    private val coordFunc: (
//        jde: Double,
//        position: DoubleArray,
//        velocity: DoubleArray,
//        userData: Any?
//    ) -> Unit,
//    private val flagLabels: Boolean,
//) : CelestialObject() {
//    private var texMap: AstralisTexture? = null
//    private var normalMap: AstralisTexture? = null
//    private var horizonMap: AstralisTexture? = null
//
//    private var sphereVAO: VertexArrayObject? = null
//    private var sphereVBO: GlBuffer? = null
//
//    private var lastJDE: Double = J2000
//    private var orbitPositionsCache = InMemoryKache<Double, Vec3d>((ORBIT_SEGMENTS * 2).toLong()) {
//        strategy = KacheStrategy.LRU
//    }
//    private val parent: Planet? = null
//    private val rotationElements = RotationElements(name)
//    private val sphereScale = 1.0  // Rotation and axis orientation parameters
//    private val axisRotation = 0f
//
//    // oneMinusOblateness = (polar radius) / (equatorial radius).
//    // Geometric flattening f = 1 - oneMinusOblateness (ExplanSup2013 10.1)
//    private val oneMinusOblateness = 1 - oblateness
//
//    private val outgasIntensity = 0f
//    private val outgasFalloff = 0f
//
//    data class RenderData(
//        var modelMatrix: Mat4d = Mat4d(),
//        var mTarget: Mat4d = Mat4d(),
//        var eyePos: Vec3d = Vec3d()
//    )
//
//    fun computeModelMatrix(solarEclipseCase: Boolean): Mat4d {
//        var result = Mat4d.translation(eclipticPos) * rotLocalToParent
//
//        var p: Planet? = parent
//        when (rotationElements.method) {
//            RotationElements.ComputationMethod.TRADITIONAL -> {
//                while (p?.parent != null) {
//                    result = Mat4d.translation(p.eclipticPos) * result * p.rotLocalToParent
//                    p = p.parent
//                }
//            }
//
//            RotationElements.ComputationMethod.WGCCRE -> {
//                while (p?.parent != null) {
//                    result = Mat4d.translation(p.eclipticPos) * result
//                    p = p.parent
//                }
//            }
//        }
//
//        // WEIRD! The following has to be disabled to have correct solar eclipse sizes in InfoString.
//        // However, it has to be active for Lunar eclipse shadow rendering.
//        // Maybe SolarSystem.getSolarEclipseFactor() can be implemented without Planet.computeModelMatrix()
//        if (name == "Moon" && !solarEclipseCase) {
//            val sun: Planet = SolarSystem.getInstance()
//                .getSun() // Assuming SolarSystem has a singleton instance and getSun method
//            val earthSunDistance = parent?.eclipticPos?.norm() ?: 0.0
//            val earthMoonDistance = eclipticPos.norm()
//            val factor = earthMoonDistance / earthSunDistance
//
//            result = Mat4d.translation((factor * sun.aberrationPush)) *
//                    result * Mat4d.zRotation(PI / 180.0 * (axisRotation + 90.0))
//        } else {
//            result = Mat4d.translation(aberrationPush) * result * Mat4d.zRotation(
//                PI / 180.0 * (axisRotation + 90.0)
//            )
//        }
//
//        return result
//    }
//
//    fun setCommonShaderUniforms(
//        painter: AstralisPainter,
//        shader: Int,
//        shaderVars: PlanetShaderVars
//    ): RenderData {
//        val data = RenderData()
//
//        val sun = SolarSystem.getInstance().getSun()
//        val projector = painter.projector
//
//        val m = projector.projectionMatrix
//        val qMat = m.toFloatArray()
//
//        data.modelMatrix = computeModelMatrix(false)
//        data.mTarget = data.modelMatrix.inverse()
//
//        val core = StelApp.getInstance().core
//        data.eyePos = core.observerHeliocentricEclipticPos
//        core.getHeliocentricEclipticModelViewTransform(StelCore.RefractionOff).forward(data.eyePos)
//        projector.modelViewTransform.backward(data.eyePos)
//        data.eyePos = data.eyePos.normalize()
//
//        GLES20.glUseProgram(shader)
//
//        GLES20.glUniformMatrix4fv(shaderVars.projectionMatrix, 1, false, qMat.toFloatArray(), 0)
//        GLES20.glUniform3f(
//            shaderVars.eyeDirection,
//            data.eyePos.x.toFloat(),
//            data.eyePos.y.toFloat(),
//            data.eyePos.z.toFloat()
//        )
//        GLES20.glUniform1i(shaderVars.tex, 0)
//
//        if (this != sun) {
//            GLES20.glUniform4f(
//                shaderVars.sunInfo,
//                data.mTarget[12].toFloat(),
//                data.mTarget[13].toFloat(),
//                data.mTarget[14].toFloat(),
//                sun.equatorialRadius.toFloat()
//            )
//        }
//
//        GLES20.glUniform2f(shaderVars.poleLat, 1.1f, -0.1f)
//
//        if (shaderVars.orenNayarParameters >= 0) {
//            val roughnessSq = roughness * roughness
//            val orenNayarParams = floatArrayOf(
//                1.0f - 0.5f * roughnessSq / (roughnessSq + 0.33f),
//                0.45f * roughnessSq / (roughnessSq + 0.09f),
//                50.0f * albedo / PI.toFloat(),
//                roughnessSq
//            )
//            GLES20.glUniform4fv(shaderVars.orenNayarParameters, 1, orenNayarParams, 0)
//        }
//
//        val outgasIntensityDistanceScaled =
//            (outgasIntensity.toDouble() / getHeliocentricEclipticPos().normSquared()).toFloat()
//        GLES20.glUniform2f(
//            shaderVars.outgasParameters,
//            outgasIntensityDistanceScaled,
//            outgasFalloff
//        )
//
//        return data
//    }
//
//    suspend fun draw(
//        core: AstralisCore,
//        eclipseFactor: Double
//    ) {
//        if (hidden) return
//
//        val ss = SolarSystem.getInstance()
//
//        val cutDimObjects = pType >= PlanetType.ASTEROID
//        if (ss.markerValue == 0.0 && cutDimObjects && !core.currentLocation.planetName.contains(
//                "Observer",
//                ignoreCase = true
//            )
//        ) {
//            return
//        }
//        if (pType >= PlanetType.ASTEROID) return
//
//        var mat = Mat4d.translation(eclipticPos) * rotLocalToParent
//        var p = parent
//
//        when (rotationElements.method) {
//            RotationElements.ComputationMethod.TRADITIONAL -> {
//                while (p?.parent != null) {
//                    mat = Mat4d.translation(p.eclipticPos) * mat * p.rotLocalToParent
//                    p = p.parent
//                }
//            }
//
//            RotationElements.ComputationMethod.WGCCRE -> {
//                while (p?.parent != null) {
//                    mat = Mat4d.translation(p.eclipticPos) * mat
//                    p = p.parent
//                }
//            }
//        }
//        mat = Mat4d.translation(aberrationPush) * mat
//
//        val transfo = core.getHeliocentricEclipticModelViewTransform()
//        transfo.combine(mat)
//
//        if (this == core.currentPlanet) {
//            if (rings) draw3dModel(core, transfo, 1024f)
//            return
//        }
//    }
//
//    suspend fun draw3dModel(
//        core: AstralisCore,
//        transfo: StelProjector.ModelViewTranformP,
//        screenRd: Float
//    ) {
//        val ssm = core.getModule(SolarSystem::class.java)
//
//        // Draw the real 3D object only if the screen radius is greater than 1.
//        if (screenRd > 1f) {
//            val n = core.clippingPlanes.first
//            val f = core.clippingPlanes.second
//
//            // Adjust clipping planes based on the planet's radius.
//            val r = equatorialRadius * sphereScale
//            val dist = getEquinoxEquatorialPos(core).norm()
//            val zNear = max(0.00001, dist - r)
//            val zFar = dist + 10 * r
//            core.setClippingPlanes(zNear, zFar)
//
//            val transfo2 = transfo.clone()
//            transfo2.combine(Mat4d.zRotation(PI / 180 * (axisRotation + 90f)))
//            val sPainter = StelPainter(core.getProjection(transfo2))
//
//            val sunPos = ssm.getSun().heliocentricEclipticPos + ssm.getSun().aberrationPush()
//            core.getHeliocentricEclipticModelViewTransform().forward(sunPos)
//
//            drawSphere(sPainter, screenRd)
//
//            core.setClippingPlanes(n, f)  // Restore original clipping planes
//        }
//    }
//
//    suspend fun drawSphere(painter: AstralisPainter, screenRd: Float) {
//        val sphereScaleF = sphereScale.toFloat()
//
//        // Проверка загрузки текстур
//        if (horizonMap?.bind(0u) != true) return
//        if (normalMap?.bind(0u) != true) return
//        if (texMap?.bind(0u) != true) return
//
//        painter.setBlending(false)
//        painter.setCullFace(true)
//
//        // Количество фасетов для оптимизации
//        val nbFacet = (screenRd * 40f / 50f * sqrt(sphereScaleF).coerceIn(10f, 100f))
//            .toUInt().toUShort()
//
//        // Генерация сферической модели
//        val model = Planet3DModel()
//        sSphere(model, equatorialRadius.toFloat(), oneMinusOblateness.toFloat(), nbFacet, nbFacet)
//
//        // Проекция вершин
//        val projectedVertexArr = FloatArray(model.vertexArr.size)
//        for (i in model.vertexArr.indices step 3) {
//            val p = Vec3f(
//                model.vertexArr[i],
//                model.vertexArr[i + 1],
//                model.vertexArr[i + 2]
//            ) * sphereScaleF
//            painter.projector.project(p, Vector3.getVectorAtIndex(i, projectedVertexArr))
//        }
//
//        // Проверка шейдеров
//        if (shaderError) return
//
//        var shader = planetShaderProgram
//        var shaderVars = planetShaderVars
//
//        if (this == SolarSystem.getMoon()) {
//            shader = moonShaderProgram
//            shaderVars = moonShaderVars
//        }
//
//        // Если шейдеры не загружены, попытка их инициализировать
//        if (shader == null) {
//            initShader()
//            if (shaderError) {
//                println("Can't use planet drawing, shaders invalid!")
//                return
//            }
//            shader = planetShaderProgram
//            if (this == SolarSystem.getMoon()) {
//                shader = moonShaderProgram
//            }
//        }
//
//        shader?.let { KglAndroid.useProgram(it) }
//
//        if (sphereVAO == null) {
//            sphereVAO = KglAndroid.createVertexArray()
//            sphereVBO = KglAndroid.createBuffer()
//        }
//
//        KglAndroid.bindVertexArray(sphereVAO)
//        KglAndroid.bindBuffer(GL_ARRAY_BUFFER, sphereVBO)
//
//        val projectedVertArrSize = projectedVertexArr.size * Float.SIZE_BYTES
//        val modelVertArrOffset = projectedVertArrSize
//        val modelVertArrSize = model.vertexArr.size * Float.SIZE_BYTES
//        val texCoordsOffset = modelVertArrOffset + modelVertArrSize
//        val texCoordsSize = model.texCoordArr.size * Float.SIZE_BYTES
//        val indicesOffset = texCoordsOffset + texCoordsSize
//        val indicesSize = model.indiceArr.size * Short.SIZE_BYTES
//
//        KglAndroid.bufferData(
//            GL_ARRAY_BUFFER,
//            FloatBuffer(0),
//            (indicesOffset + indicesSize),
//            GL_STREAM_DRAW
//        )
//        KglAndroid.bufferSubData(
//            GL_ARRAY_BUFFER,
//            0,
//            projectedVertArrSize,
//            FloatBuffer(projectedVertexArr)
//        )
//        KglAndroid.bufferSubData(
//            GL_ARRAY_BUFFER,
//            modelVertArrOffset,
//            modelVertArrSize,
//            FloatBuffer(model.vertexArr)
//        )
//        KglAndroid.bufferSubData(
//            GL_ARRAY_BUFFER,
//            texCoordsOffset,
//            texCoordsSize,
//            FloatBuffer(model.texCoordArr)
//        )
//        KglAndroid.bufferSubData(
//            GL_ARRAY_BUFFER,
//            indicesOffset,
//            indicesSize,
//            IntBuffer(model.indiceArr.map { it.toInt() }.toIntArray())
//        )
//
//        KglAndroid.setAttributeBuffer(shaderVars.vertex, GL_FLOAT, 0, 3)
//        KglAndroid.enableAttributeArray(shaderVars.vertex)
//        KglAndroid.setAttributeBuffer(shaderVars.unprojectedVertex, GL_FLOAT, modelVertArrOffset, 3)
//        KglAndroid.enableAttributeArray(shaderVars.unprojectedVertex)
//        KglAndroid.setAttributeBuffer(shaderVars.texCoord, GL_FLOAT, texCoordsOffset, 2)
//        KglAndroid.enableAttributeArray(shaderVars.texCoord)
//
//        GLES20.glDrawElements(
//            GL_TRIANGLES,
//            model.indiceArr.size,
//            GL_UNSIGNED_SHORT,
//            indicesOffset
//        )
//
//        KglAndroid.bindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
//        KglAndroid.bindBuffer(GL_ARRAY_BUFFER, 0)
//        KglAndroid.bindVertexArray(0)
//
//        KglAndroid.useProgram(0)
//        painter.setCullFace(false)
//    }
//
//    // A Planet's own eclipticPos is in VSOP87 ref. frame (practically equal to ecliptic of J2000 for us) coordinates relative to the parent body (sun, planet).
//    // To get J2000 equatorial coordinates, we require heliocentric ecliptical positions (adding up parent positions) of observer and Planet.
//    // Then we use the matrix rotation multiplication with an existing matrix in StelCore to orient from eclipticalJ2000 to equatorialJ2000.
//    // The end result is a non-normalized 3D vector which allows retrieving distances etc.
//    // To apply aberration correction, we need the velocity vector of the observer's planet and apply a little correction in SolarSystem::computePositions()
//    // prepare for aberration: Explan. Suppl. 2013, (7.38)
//    override fun getJ2000EquatorialPos(core: AstralisCore): Vec3d {
//        return AstralisCore.matVsop87ToJ2000.multiplyWithoutTranslation(
//            getHeliocentricEclipticPos() - core.getObserverHeliocentricEclipticPos() + Vec3d(0.0)
//        )
//    }
//
//
//    fun getHeliocentricEclipticPos(): Vec3d = getHeliocentricPos(eclipticPos)
//
//
////    suspend fun getHeliocentricEclipticPos(dateJDE: Double): Vec3d {
////        var pos = getEclipticPos(dateJDE)
////
////        var parentPlanet = parent
////        if (parentPlanet != null) {
////            while (parentPlanet?.parent != null) {
////                pos += parentPlanet.getEclipticPos(dateJDE)
////                parentPlanet = parentPlanet.parent
////            }
////        }
////        return pos
////    }
//
//    // Return heliocentric ecliptical Cartesian J2000 coordinates of p [AU]
//    fun getHeliocentricPos(p: Vec3d): Vec3d {
//        var pos = p
//        var pp = parent
//
//        if (pp != null) {
//            while (pp?.parent != null) {
//                pos += pp.eclipticPos
//                pp = pp.parent
//            }
//        }
//        return pos
//    }
//
//    // Получение позиции планеты в эклиптических координатах (J2000) в а. е., центрированной на родительской планете
//    suspend fun getEclipticPos(dateJDE: Double = lastJDE): Vec3d {
//        // Используем текущую позицию, если время совпадает
//        if (fuzzyEquals(dateJDE, lastJDE)) {
//            return eclipticPos
//        }
//
//        // В противном случае пытаемся использовать кэшированную позицию
//        val cachedPos = orbitPositionsCache.get(dateJDE)
//        if (cachedPos != null) {
//            return cachedPos
//        }
//
//        // Если позиции нет в кэше, вычисляем её
//        val pos = Vec3d()
//        val velDummy = Vec3d()
//        coordFunc(dateJDE, pos.toDoubleArray(), velDummy.toDoubleArray(), null)
//
//        // Сохраняем в кэш и возвращаем
//        orbitPositionsCache.put(dateJDE, pos)
//        return pos
//    }
//
//    companion object {
//        var shaderError = false
//        var planetShaderProgram: Shader? = null
//        var planetShaderVars = PlanetShaderVars()
//        var moonShaderProgram: Shader? = null
//        var moonShaderVars = PlanetShaderVars()
//        var transformShaderProgram: Shader? = null
//        var transformShaderVars = PlanetShaderVars()
//
//        fun createShader(
//            name: String,
//            vars: PlanetShaderVars,
//            vSrc: String,
//            fSrc: String,
//            prefix: String = "",
//            fixedAttributeLocations: Map<String, AttributeLocation> = emptyMap()
//        ): Shader? {
//            val program = KglAndroid.createProgram()
//            if (program == 0 || program == null) {
//                Logger.e("Planet") { "Cannot create shader program object for $name" }
//                return null
//            }
//
//            fun compileShader(type: Int, source: String, shaderName: String): Int? {
//                val shader = KglAndroid.createShader(type)
//                if (shader == 0 || shader == null) {
//                    Logger.e("Planet") { "$shaderName could not be created" }
//                    return null
//                }
//
//                KglAndroid.shaderSource(shader, prefix + source)
//                KglAndroid.compileShader(shader)
//
//                val compileStatus = KglAndroid.getShaderParameter(shader, GL_COMPILE_STATUS)
//                if (compileStatus == 0) {
//                    val log = KglAndroid.getShaderInfoLog(shader)
//                    Logger.e("Planet") { "$name $shaderName compilation failed: $log" }
//                    KglAndroid.deleteShader(shader)
//                    return null
//                }
//
//                return shader
//            }
//
//            val vertexShader = if (vSrc.isNotEmpty()) compileShader(
//                GL_VERTEX_SHADER,
//                vSrc,
//                "vertex shader"
//            ) else null
//            val fragmentShader = if (fSrc.isNotEmpty()) compileShader(
//                GL_FRAGMENT_SHADER,
//                fSrc,
//                "fragment shader"
//            ) else null
//
//            vertexShader?.let { KglAndroid.attachShader(program, it) }
//            fragmentShader?.let { KglAndroid.attachShader(program, it) }
//
//            // Привязка фиксированных атрибутов
//            for ((attr, location) in fixedAttributeLocations) {
//                KglAndroid.bindAttribLocation(program, location.ordinal, attr)
//            }
//
//            // Линковка шейдерной программы
//            KglAndroid.linkProgram(program)
//            val linkStatus = KglAndroid.getProgramParameter(program, GL_LINK_STATUS)
//
//            if (linkStatus == 0) {
//                val log = KglAndroid.getProgramInfoLog(program)
//                Logger.e("Planet") { "$name shader program linking failed: $log" }
//                KglAndroid.deleteProgram(program)
//                return null
//            }
//
//            vars.initLocations(program)
//
//            // Очистка памяти: удаление ненужных уже прикрепленных шейдеров
//            vertexShader?.let { KglAndroid.deleteShader(it) }
//            fragmentShader?.let { KglAndroid.deleteShader(it) }
//
//            return program
//        }
//
//        @OptIn(ExperimentalResourceApi::class)
//        suspend fun initShader(): Boolean {
//            if (planetShaderProgram != null || shaderError) return !shaderError // Уже выполнено.
//
//            Logger.d("Shader") { "Initializing planets GL shaders..." }
//            shaderError = true
//
//            // Загрузка шейдеров из файлов
//            val vBytes = Res.readBytes("files/shaders/planet.vert")
//            val fBytes = Res.readBytes("files/shaders/planet.frag")
//
//            val vsrc = vBytes.decodeToString()
//            val fsrc = fBytes.decodeToString()
//
//            shaderError = false
//
//            // Создание шейдеров
//            planetShaderProgram = createShader(
//                "planetShaderProgram",
//                planetShaderVars,
//                vsrc,
//                makeSRGBUtilsShader() + fsrc
//            )
//            moonShaderProgram = createShader(
//                "moonShaderProgram",
//                moonShaderVars,
//                vsrc,
//                makeSRGBUtilsShader() + fsrc,
//                "#define IS_MOON\n\n"
//            )
//
//            val attrLoc = mapOf(
//                "unprojectedVertex" to AttributeLocation.ATTLOC_VERTEX,
//                "texCoord" to AttributeLocation.ATTLOC_TEXCOORD,
//                "normalIn" to AttributeLocation.ATTLOC_NORMAL
//            )
//
//            // Трансформационный шейдер (используется для depth map)
//            val transformVShader = """
//            uniform mat4 projectionMatrix;
//            attribute vec4 unprojectedVertex;
//            void main() {
//                gl_Position = projectionMatrix * unprojectedVertex;
//            }
//            """.trimIndent()
//
//            var transformFShader = "void main() { }"
//
//            transformShaderProgram = createShader(
//                "transformShaderProgram",
//                transformShaderVars,
//                transformVShader,
//                transformFShader,
//                "",
//                attrLoc
//            )
//
//            // Проверка создания всех шейдеров
//            shaderError = !(planetShaderProgram != null && moonShaderProgram != null &&
//                    transformShaderProgram != null)
//
//            return true
//        }
//
//        class PlanetShaderVars {
//            // Vertex attributes
//            var texCoord: Int = -1
//            var unprojectedVertex: Int = -1
//            var vertex: Int = -1
//            var normalIn: Int = -1
//
//            // Common uniforms
//            var projectionMatrix: Int = -1
//            var hasAtmosphere: Int = -1
//            var tex: Int = -1
//            var lightDirection: Int = -1
//            var eyeDirection: Int = -1
//            var diffuseLight: Int = -1
//            var ambientLight: Int = -1
//            var shadowCount: Int = -1
//            var shadowData: Int = -1
//            var sunInfo: Int = -1
//            var skyBrightness: Int = -1
//            var orenNayarParameters: Int = -1
//            var outgasParameters: Int = -1
//
//            // For Mars poles
//            var poleLat: Int = -1
//
//            // Moon-specific variables
//            var earthShadow: Int = -1
//            var eclipsePush: Int = -1
//            var normalMap: Int = -1
//            var horizonMap: Int = -1
//
//            // Rings-specific variables
//            var isRing: Int = -1
//            var ring: Int = -1
//            var outerRadius: Int = -1
//            var innerRadius: Int = -1
//            var ringS: Int = -1
//
//            // Shadowmap variables
//            var shadowMatrix: Int = -1
//            var shadowTex: Int = -1
//            var poissonDisk: Int = -1
//
//            fun initLocations(program: Int) {
//                GLES20.glUseProgram(program)
//
//                // Attributes
//                texCoord = KglAndroid.getAttribLocation(program, "texCoord")
//                unprojectedVertex = KglAndroid.getAttribLocation(program, "unprojectedVertex")
//                vertex = KglAndroid.getAttribLocation(program, "vertex")
//                normalIn = KglAndroid.getAttribLocation(program, "normalIn")
//
//                // Common uniforms
//                projectionMatrix = KglAndroid.getUniformLocation(program, "projectionMatrix") ?: -1
//                tex = KglAndroid.getUniformLocation(program, "tex") ?: -1
//                poleLat = KglAndroid.getUniformLocation(program, "poleLat") ?: -1
//                lightDirection = KglAndroid.getUniformLocation(program, "lightDirection") ?: -1
//                eyeDirection = KglAndroid.getUniformLocation(program, "eyeDirection") ?: -1
//                diffuseLight = KglAndroid.getUniformLocation(program, "diffuseLight") ?: -1
//                ambientLight = KglAndroid.getUniformLocation(program, "ambientLight") ?: -1
//                shadowCount = KglAndroid.getUniformLocation(program, "shadowCount") ?: -1
//                shadowData = KglAndroid.getUniformLocation(program, "shadowData") ?: -1
//                sunInfo = KglAndroid.getUniformLocation(program, "sunInfo") ?: -1
//                skyBrightness = KglAndroid.getUniformLocation(program, "skyBrightness") ?: -1
//                orenNayarParameters =
//                    KglAndroid.getUniformLocation(program, "orenNayarParameters") ?: -1
//                outgasParameters = KglAndroid.getUniformLocation(program, "outgasParameters") ?: -1
//                hasAtmosphere = KglAndroid.getUniformLocation(program, "hasAtmosphere") ?: -1
//
//                // Moon-specific variables
//                earthShadow = KglAndroid.getUniformLocation(program, "earthShadow") ?: -1
//                eclipsePush = KglAndroid.getUniformLocation(program, "eclipsePush") ?: -1
//                normalMap = KglAndroid.getUniformLocation(program, "normalMap") ?: -1
//                horizonMap = KglAndroid.getUniformLocation(program, "horizonMap") ?: -1
//
//                // Rings-specific variables
//                isRing = KglAndroid.getUniformLocation(program, "isRing") ?: -1
//                ring = KglAndroid.getUniformLocation(program, "ring") ?: -1
//                outerRadius = KglAndroid.getUniformLocation(program, "outerRadius") ?: -1
//                innerRadius = KglAndroid.getUniformLocation(program, "innerRadius") ?: -1
//                ringS = KglAndroid.getUniformLocation(program, "ringS") ?: -1
//
//                // Shadowmap variables
//                shadowMatrix = KglAndroid.getUniformLocation(program, "shadowMatrix") ?: -1
//                shadowTex = KglAndroid.getUniformLocation(program, "shadowTex") ?: -1
//                poissonDisk = KglAndroid.getUniformLocation(program, "poissonDisk") ?: -1
//
//                GLES20.glUseProgram(0)
//            }
//        }
//    }
//
//    // Вычисляет осевое вращение вокруг Z (суточное вращение вокруг полярной оси) [в градусах]
//    // для перехода от экваториальных координат к координатам часового угла.
//    // Для Земли звездное время — это угол вдоль экватора планеты от RA0 до меридиана,
//    // то есть часовой угол первой точки Овна. Для Земли это звездное время в Гринвиче.
//    //
//    // В этом контексте вычисляется угол W главного меридиана от восходящего узла экватора планеты на экваторе ICRF.
//    //
//    // Нам нужны и JD, и JDE для Земли (для других планет только JDE).
//    fun getSiderealTime(JD: Double, JDE: Double): Double {
//        if (name == "Earth") {
//            return if (StelApp.getInstance().core.useNutation) {
//                getApparentSiderealTime(JD, JDE) // В градусах
//            } else {
//                getMeanSiderealTime(JD, JDE) // В градусах
//            }
//        }
//
//        // V0.21+: новые значения вращения из ExplSup2013 или WGCCRE2009/2015
//        if (rotationElements.method == RotationElements.ComputationMethod.WGCCRE) {
//            // Возвращает угол W — долготу главного меридиана, измеренную вдоль экватора планеты
//            // от восходящего узла экватора планеты на экваторе ICRF.
//            val t = JDE - J2000
//            val T = t / 36525.0
//            var w =
//                rotationElements.W0 + (t * rotationElements.W1) % 360.0 // Ограничиваем угол, чтобы избежать переполнения
//            w += rotationElements.corrW(
//                t,
//                T
//            ) // Применяем специфические поправки (ExplSup2013/WGCCRE)
//            return w
//        }
//
//        // СТАРАЯ МОДЕЛЬ (до V0.21)
//        // Используется для объектов, у которых нет современных данных WGCCRE.
//
//        val t = JDE - rotationElements.epoch
//        // Избегаем деления на ноль (для лун с хаотичным периодом вращения)
//        val rotations = if (rotationElements.period == 0.0) {
//            1.0 // Луна с хаотичным периодом вращения
//        } else {
//            (t / rotationElements.period) % 1.0 // Ограничиваем число полных оборотов
//        }
//
//        return rotations * 360.0 + rotationElements.offset
//    }
//}
//
//// Get observer-centered alt/az position
////fun CelestialObject.getAltAzPosAuto(val core: AstralisCore): Vec3d {
////    return core.j2000ToAltAz(getJ2000EquatorialPos(core), StelCore::RefractionAuto)
////}
//
//fun sSphere(
//    model: Planet3DModel,
//    radius: Float,
//    oneMinusOblateness: Float,
//    slices: UShort,
//    stacks: UShort
//) {
//    val vertexList = mutableListOf<Float>()
//    val texCoordList = mutableListOf<Float>()
//    val indiceList = mutableListOf<UShort>()
//
//    var s: Float
//    var t = 1f
//
//    val cosSinRho = computeCosSinRho(stacks.toInt())
//    val cosSinTheta = computeCosSinTheta(slices.toInt())
//
//    val ds = 1f / slices.toFloat()
//    val dt = 1f / stacks.toFloat()
//
//    var cosSinRhoPIndex = 0
//    for (i in 0 until stacks.toInt()) {
//        s = 0f
//        var cosSinThetaPIndex = 0
//        for (j in 0..slices.toInt()) {
//            var x = -cosSinTheta[cosSinThetaPIndex + 1] * cosSinRho[cosSinRhoPIndex + 1]
//            var y = cosSinTheta[cosSinThetaPIndex] * cosSinRho[cosSinRhoPIndex + 1]
//            var z = cosSinRho[cosSinRhoPIndex]
//
//            texCoordList.add(s)
//            texCoordList.add(t)
//            vertexList.add(x * radius)
//            vertexList.add(y * radius)
//            vertexList.add(z * oneMinusOblateness * radius)
//
//            x = -cosSinTheta[cosSinThetaPIndex + 1] * cosSinRho[cosSinRhoPIndex + 3]
//            y = cosSinTheta[cosSinThetaPIndex] * cosSinRho[cosSinRhoPIndex + 3]
//            z = cosSinRho[cosSinRhoPIndex + 2]
//
//            texCoordList.add(s)
//            texCoordList.add(t - dt)
//            vertexList.add(x * radius)
//            vertexList.add(y * radius)
//            vertexList.add(z * oneMinusOblateness * radius)
//
//            s += ds
//            cosSinThetaPIndex += 2
//        }
//
//        val offset = i * (slices.toInt() + 1) * 2
//        val limit = slices.toInt() * 2 + 2
//
//        for (j in 2 until limit step 2) {
//            indiceList.add((offset + j - 2).toUShort())
//            indiceList.add((offset + j - 1).toUShort())
//            indiceList.add((offset + j).toUShort())
//            indiceList.add((offset + j).toUShort())
//            indiceList.add((offset + j - 1).toUShort())
//            indiceList.add((offset + j + 1).toUShort())
//        }
//
//        t -= dt
//        cosSinRhoPIndex += 2
//    }
//
//    // Записываем данные обратно в массивы модели
//    model.vertexArr = vertexList.toFloatArray()
//    model.texCoordArr = texCoordList.toFloatArray()
//    model.indiceArr = indiceList.toUShortArray()
//}
