#ifndef MESH_ANIMATION_SHADER_H_
#define MESH_ANIMATION_SHADER_H_

#include <memory>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/animation/mesh_animation.h"
#include "objects/recyclable_object.h"

namespace gvr {

class GLProgram;
class RenderData;
class Material;

class MeshAnimationShader: public RecyclableObject {
public:
    MeshAnimationShader();
    ~MeshAnimationShader();
    void recycle();
    void render(const glm::mat4& mvp_matrix, RenderData* render_data, Material* material);

private:
    MeshAnimationShader(const MeshAnimationShader& mesh_animation_shader);
    MeshAnimationShader(MeshAnimationShader&& mesh_animation_shader);
    MeshAnimationShader& operator=(const MeshAnimationShader& mesh_animation_shader);
    MeshAnimationShader& operator=(MeshAnimationShader&& mesh_animation_shader);

private:
    GLProgram* program_;
    GLuint u_mvp_;
    GLuint u_texture_;
    GLuint bones_transforms_ids_[MAX_BONES];
};

}

#endif
