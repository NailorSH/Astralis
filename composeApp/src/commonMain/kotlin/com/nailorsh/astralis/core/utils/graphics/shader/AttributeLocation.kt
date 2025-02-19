package com.nailorsh.astralis.core.utils.graphics.shader

// Specifies the attribute location used for the glVertexAttribPointer calls.
// Shaders should be configured to use these locations before linking,
// so that a single vertex format can be used for all shaders.
enum class AttributeLocation {
    // This is the OpenGL attribute location where 3D vertex positions are mapped to
    ATTLOC_VERTEX,

    // This is the OpenGL attribute location where 2D vertex texture coordinates are mapped to
    ATTLOC_TEXCOORD,

    // This is the OpenGL attribute location where 3D vertex normals are mapped to
    ATTLOC_NORMAL,

    // This is the OpenGL attribute location where vertex tangents are mapped to
    ATTLOC_TANGENT,

    // This is the OpenGL attribute location where vertex bitangents are mapped to
    ATTLOC_BITANGENT,

    // The attribute locations starting with this index are unused, and should be used for custom
    // vertex attributes not supported by this class
    ATTLOC_SIZE
}