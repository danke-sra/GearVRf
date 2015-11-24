#include "mesh_animation_shader.h"

#include "gl/gl_program.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "objects/components/render_data.h"
#include "objects/textures/texture.h"
#include "util/gvr_gl.h"

#include "util/gvr_log.h"

namespace gvr {

static const char VERTEX_SHADER[] =
        "attribute vec4 a_position;\n"
        "attribute vec4 a_tex_coord;\n"
        "attribute vec4 a_bone_id;\n"
        "attribute vec4 a_bone_weight;\n"
        "attribute vec3 a_normal;\n"
        "varying vec2 v_tex_coord;\n"
        "uniform mat4 u_mvp;\n"
        "\n"
        "const int MAX_BONES = 100;\n"
        "uniform mat4 bones[MAX_BONES];\n"
        "\n"
        "void main() {\n"
        "    ivec4 i_a_bone_id = ivec4(a_bone_id);\n"
        "\n"
        "    mat4 bone_transform = bones[i_a_bone_id[0]] * a_bone_weight[0];\n"
        "    bone_transform += bones[i_a_bone_id[1]] * a_bone_weight[1];\n"
        "    bone_transform += bones[i_a_bone_id[2]] * a_bone_weight[2];\n"
        "    bone_transform += bones[i_a_bone_id[3]] * a_bone_weight[3];\n"
        "\n"
        "    vec4 position_from_bone = bone_transform * a_position;\n"
        "\n"
        "    gl_Position = u_mvp * position_from_bone;\n"
        "    v_tex_coord = a_tex_coord.xy;\n"
        "}\n";

static const char FRAGMENT_SHADER[] =
        "precision mediump float;\n"
        "uniform sampler2D u_texture;\n"
        "varying vec2 v_tex_coord;\n"
        "\n"
        "void main() {\n"
        "    gl_FragColor = texture2D(u_texture, v_tex_coord);\n"
        "}\n";

MeshAnimationShader::MeshAnimationShader() :
        program_(0), u_mvp_(0), u_texture_(0) {
    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);

    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");

    for (int i = 0; i < MAX_BONES; i++) {
        char name[128];
        memset(name, 0, sizeof(name));
        snprintf(name, sizeof(name), "bones[%d]", i);

        bones_transforms_ids_[i] = glGetUniformLocation(program_->id(), name);

        if (bones_transforms_ids_[i] == 0xffffffff) {
            LOGD("Warning! Unable to get the location of uniform '%s'\n", name);
        }
    }
}

MeshAnimationShader::~MeshAnimationShader() {
    if (program_ != 0) {
        recycle();
    }
}

void MeshAnimationShader::recycle() {
    delete program_;
    program_ = 0;
}

void MeshAnimationShader::render(const glm::mat4& mvp_matrix, RenderData* render_data,
        Material* material) {
    Mesh* mesh = render_data->mesh();
    Texture* texture = material->getTexture("main_texture");

#if _GVRF_USE_GLES3_
    glUseProgram(program_->id());

    mesh->generateVAO();

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(mvp_matrix));
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(texture->getTarget(), texture->getId());
    glUniform1i(u_texture_, 0);

    MeshAnimation meshAnimation = mesh->getMeshAnimation();
    glm::mat4 finalTransform;

    for (unsigned int i = 0; i < meshAnimation.numberOfBones(); i++) {
        finalTransform = meshAnimation.getBoneInfo()[i].finalTransform;
        glUniformMatrix4fv(bones_transforms_ids_[i], 1, GL_TRUE, glm::value_ptr(finalTransform));
    }

    glBindVertexArray(mesh->getVAOId(Material::MESH_ANIMATION_SHADER));
    glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT, 0);
    glBindVertexArray(0);
#else
    glUseProgram(program_->id());

    glVertexAttribPointer(a_position_, 3, GL_FLOAT, GL_FALSE, 0, mesh->vertices().data());
    glEnableVertexAttribArray(a_position_);

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(mvp_matrix));

    MeshAnimation meshAnimation = mesh->getMeshAnimation();
    glm::mat4 finalTransform;

    for (unsigned int i = 0; i < meshAnimation.numberOfBones(); i++) {
        finalTransform = meshAnimation.getBoneInfo()[i].finalTransform;
        glUniformMatrix4fv(bones_transforms_ids_[i], 1, GL_TRUE, glm::value_ptr(finalTransform));
    }

    glActiveTexture (GL_TEXTURE0);
    glBindTexture(texture->getTarget(), texture->getId());
    glUniform1i(u_texture_, 0);

    glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT,
            mesh->indices().data());
#endif

    checkGlError("MeshAnimationShader::render");
}

}
