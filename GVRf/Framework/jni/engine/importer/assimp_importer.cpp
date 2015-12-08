/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/***************************************************************************
 * Contains a ai_scene of Assimp.
 ***************************************************************************/

#include "assimp_importer.h"

#include <glm/gtc/type_ptr.hpp>

#include "objects/mesh.h"
#include "objects/animation/mesh_animation.h"

namespace gvr {
Mesh* AssimpImporter::getMesh(int index) {
    Mesh* mesh = new Mesh();

    if (assimp_importer_->GetScene() == 0) {
        LOGE("_ASSIMP_SCENE_NOT_FOUND_");
        delete mesh;
        return 0;
    }

    aiMesh* ai_mesh = assimp_importer_->GetScene()->mMeshes[index];

    std::vector<glm::vec3> vertices;
    for (int i = 0; i < ai_mesh->mNumVertices; ++i) {
        vertices.push_back(
                glm::vec3(ai_mesh->mVertices[i].x, ai_mesh->mVertices[i].y,
                        ai_mesh->mVertices[i].z));
    }
    mesh->set_vertices(std::move(vertices));

    if (ai_mesh->mNormals != 0) {
        std::vector<glm::vec3> normals;
        for (int i = 0; i < ai_mesh->mNumVertices; ++i) {
            normals.push_back(
                    glm::vec3(ai_mesh->mNormals[i].x, ai_mesh->mNormals[i].y,
                            ai_mesh->mNormals[i].z));
        }
        mesh->set_normals(std::move(normals));
    }

    if (ai_mesh->mTextureCoords[0] != 0) {
        std::vector<glm::vec2> tex_coords;
        for (int i = 0; i < ai_mesh->mNumVertices; ++i) {
            tex_coords.push_back(
                    glm::vec2(ai_mesh->mTextureCoords[0][i].x,
                            ai_mesh->mTextureCoords[0][i].y));
        }
        mesh->set_tex_coords(std::move(tex_coords));
    }

    // TODO: Change this to a better approach of getting the aiScene
    MeshAnimation meshAnimation;
    meshAnimation.setScene(assimp_importer_->GetScene());

    glm::mat4 globalInverseMatrix =
            glm::make_mat4(&(assimp_importer_->GetScene()->mRootNode->mTransformation.a1));
    meshAnimation.setGlobalInverseTransform(globalInverseMatrix);
    meshAnimation.resizeBonesVector(ai_mesh->mNumVertices);

    // Load all bones information from the Assimp mesh
    if (ai_mesh->HasBones()) {
        meshAnimation.loadBones(ai_mesh);
    }

    mesh->setMeshAnimation(meshAnimation);

    std::vector<unsigned short> triangles;
    for (int i = 0; i < ai_mesh->mNumFaces; ++i) {
        if (ai_mesh->mFaces[i].mNumIndices == 3) {
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[1]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);
        } else if (ai_mesh->mFaces[i].mNumIndices == 4) {
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[1]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);

            triangles.push_back(ai_mesh->mFaces[i].mIndices[2]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[3]);
            triangles.push_back(ai_mesh->mFaces[i].mIndices[0]);
        }
    }
    mesh->set_triangles(std::move(triangles));

    return mesh;
}
}
