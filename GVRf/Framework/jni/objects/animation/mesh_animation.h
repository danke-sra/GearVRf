#ifndef MESH_ANIMATION_H_
#define MESH_ANIMATION_H_

#include <map>
#include <vector>
#include <string>

#include "assimp/mesh.h"
#include "assimp/scene.h"

#include <glm/glm.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include "objects/vertex_bone_data.h"

namespace gvr {

struct BoneInfo {
    glm::mat4 boneOffset;
    glm::mat4 finalTransform;

    BoneInfo() {
        boneOffset = glm::mat4(0.0f);
        finalTransform = glm::mat4(0.0f);
    }
};

struct BoneData {
    int ids[BONES_PER_VERTEX];
    float weights[BONES_PER_VERTEX];

    BoneData() {
        reset();
    }

    void reset() {
        for(int i = 0; i < BONES_PER_VERTEX; i++) {
            ids[i] = 0;
            weights[i]=0;
        }
    }

    void addBoneData(int boneId, float boneWeight);
};

class MeshAnimation {

public:
    MeshAnimation()
        : num_bones_(0), bones_(), bone_info_(), bone_mapping_(),
          global_inverse_transform_(), aiScene_(NULL)
    {
    }

    ~MeshAnimation() {
        // TODO: Unset/Free aiScene object
        // aiScene_ = NULL;           ????????????????????????
    }

    void setScene(const aiScene* scene);

    void resizeBonesVector(int size);
    void setGlobalInverTransform(const glm::mat4& globalTransform);

    void loadBones(const aiMesh *mesh);

    void update(float timeInSeconds);

    bool hasBones() {
        return (num_bones_ > 0);
    }

    int numberOfBones() {
        return num_bones_;
    }

    const std::vector<BoneData>& getBones() {
        return bones_;
    }

    const std::vector<BoneInfo>& getBoneInfo() {
        return bone_info_;
    }

private:
    void recurseNodes(float animationTime, const aiNode* parentNode,
            const glm::mat4& parentTransform);
    const aiNodeAnim* findNodeAnim(const aiAnimation* animation, const std::string nodeName);
    void getScalingFromTime(aiVector3D& scaling, float animationTime,
            const aiNodeAnim* nodeAnimation);
    void getRotationFromTime(aiQuaternion& rotation, float animationTime,
            const aiNodeAnim* nodeAnimation);
    void getTranslationFromTime(aiVector3D& translation, float animationTime,
            const aiNodeAnim* nodeAnimation);
    int getScalingIndex(float animationTime, const aiNodeAnim* nodeAnimation);
    int getRotationIndex(float animationTime, const aiNodeAnim* nodeAnimation);
    int getTranslationIndex(float animationTime, const aiNodeAnim* nodeAnimation);

private:
    int num_bones_;
    std::vector<BoneData> bones_;
    std::vector<BoneInfo> bone_info_;
    std::map<std::string, int> bone_mapping_;
    glm::mat4 global_inverse_transform_;
    const aiScene* aiScene_;
};

}

#endif
