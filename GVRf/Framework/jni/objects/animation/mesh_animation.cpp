#include "mesh_animation.h"

#include "util/gvr_log.h"

#include <glm/glm.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>

namespace gvr {

void BoneData::addBoneData(int boneId, float boneWeight) {

    for (int i = 0; i < sizeof(ids); i++) {
        if (weights[i] == 0.0) {
            ids[i] = boneId;
            weights[i] = boneWeight;
            return;
        }
    }
}

void MeshAnimation::setScene(const aiScene* scene) {
    aiScene_ = scene;
}

void MeshAnimation::setGlobalInverseTransform(const glm::mat4& globalTransform) {
    global_inverse_transform_ = glm::inverse(globalTransform);
}

void MeshAnimation::resizeBonesVector(int size) {
    bones_.resize(bones_.size() + size);
}

void MeshAnimation::loadBones(const aiMesh *mesh) {
    int bonesCount = 0;
    int boneIndex;
    std::string boneName;
    int vertexIndex;
    float weight;

    for (int i = 0; i < mesh->mNumBones; i++) {
        boneIndex = 0;
        boneName = mesh->mBones[i]->mName.data;

        if (bone_mapping_.find(boneName) == bone_mapping_.end()) {
            boneIndex = num_bones_;
            num_bones_ += 1;

            BoneInfo boneInfo;
            bone_info_.push_back(boneInfo);
            bone_info_[boneIndex].boneOffset = glm::make_mat4(&(mesh->mBones[i]->mOffsetMatrix.a1));

            bone_mapping_[boneName] = boneIndex;
        } else {
            boneIndex = bone_mapping_[boneName];
        }

        for (int j = 0; j < mesh->mBones[i]->mNumWeights; j++) {
            vertexIndex = mesh->mBones[i]->mWeights[j].mVertexId;
            weight = mesh->mBones[i]->mWeights[j].mWeight;

            bones_[vertexIndex].addBoneData(boneIndex, weight);
        }

        bonesCount++;

    }

    LOGD("Loaded %i bones from the Assimp mesh.", bonesCount);
}

void MeshAnimation::update(float timeInSeconds) {
    if (aiScene_ == NULL) {
        return;
    }

    glm::mat4 identityMatrix;
    float ticksPerSecond;
    float timeInTicksPerSecond;

    if (aiScene_->mAnimations[0]->mTicksPerSecond != 0) {
        ticksPerSecond = (float) aiScene_->mAnimations[0]->mTicksPerSecond;
    } else {
        ticksPerSecond = 25.0f;
    }
    timeInTicksPerSecond = timeInSeconds * ticksPerSecond;

    float animationTime;
    animationTime = fmod(timeInTicksPerSecond, aiScene_->mAnimations[0]->mDuration);

    recurseNodes(animationTime, aiScene_->mRootNode, identityMatrix);
}

void MeshAnimation::recurseNodes(float animationTime, const aiNode* node,
        const glm::mat4& parentTransform) {
    if (aiScene_ == NULL) {
        return;
    }

    std::string nodeName = node->mName.data;
    const aiAnimation* animation = aiScene_->mAnimations[0];
    const aiNodeAnim* nodeAnimation = findNodeAnim(animation, nodeName);

    glm::mat4 nodeTransform = glm::make_mat4(&node->mTransformation.a1);

    if (nodeAnimation) {
        aiVector3D scaling;
        getScalingFromTime(scaling, animationTime, nodeAnimation);
        glm::mat4 scalingMatrix;
        scalingMatrix = glm::scale(scalingMatrix, glm::vec3(scaling.x, scaling.y, scaling.z));

        aiQuaternion rotation;
        getRotationFromTime(rotation, animationTime, nodeAnimation);
        aiMatrix4x4 aiMatrixFromQuaternion(rotation.GetMatrix());
        glm::mat4 rotationMatrix;
        rotationMatrix = glm::make_mat4(&aiMatrixFromQuaternion.a1);

        aiVector3D translation;
        getTranslationFromTime(translation, animationTime, nodeAnimation);
        glm::mat4 translationMatrix;
        translationMatrix = glm::translate(translationMatrix,
                glm::vec3(translation.x, translation.y, translation.z));
        translationMatrix = glm::transpose(translationMatrix);

        // Combine all the transformations above
        nodeTransform = scalingMatrix * rotationMatrix * translationMatrix;
    }

    glm::mat4 globalTransform = nodeTransform * parentTransform;
    int boneIndex;

    if (bone_mapping_.find(nodeName) != bone_mapping_.end()) {
        boneIndex = bone_mapping_[nodeName];
        bone_info_[boneIndex].finalTransform = bone_info_[boneIndex].boneOffset * globalTransform
                * global_inverse_transform_;
    }

    for (int x = 0; x < node->mNumChildren; x++) {
        recurseNodes(animationTime, node->mChildren[x], globalTransform);
    }
}

const aiNodeAnim* MeshAnimation::findNodeAnim(const aiAnimation* animation,
        const std::string nodeName) {

    for (int i = 0; i < animation->mNumChannels; i++) {
        const aiNodeAnim* nodeAnimation = animation->mChannels[i];

        if (std::string(nodeAnimation->mNodeName.data) == nodeName) {
            return nodeAnimation;
        }
    }

    return NULL;
}

void MeshAnimation::getScalingFromTime(aiVector3D& scaling, float animationTime,
        const aiNodeAnim* nodeAnimation) {

    if (nodeAnimation->mNumScalingKeys == 1) {
        scaling = nodeAnimation->mScalingKeys[0].mValue;
        return;
    }

    int index = getScalingIndex(animationTime, nodeAnimation);
    int nextIndex = index + 1;

    if ((nodeAnimation->mScalingKeys[index].mTime <= animationTime)
        && (animationTime <= nodeAnimation->mScalingKeys[nextIndex].mTime)) {

        float deltaTime = (float) (nodeAnimation->mScalingKeys[nextIndex].mTime
                - nodeAnimation->mScalingKeys[index].mTime);
        float factor = (animationTime - (float) nodeAnimation->mScalingKeys[index].mTime)
                / deltaTime;

        const aiVector3D& start =  nodeAnimation->mScalingKeys[index].mValue;
        const aiVector3D& end = nodeAnimation->mScalingKeys[nextIndex].mValue;
        aiVector3D delta = end - start;

        scaling = start + factor * delta;
    } else {
        float firstFrameTime = nodeAnimation->mScalingKeys[0].mTime;
        float lastFrameTime =
                nodeAnimation->mScalingKeys[nodeAnimation->mNumScalingKeys - 1].mTime;
        const aiVector3D& firstFrameValue = nodeAnimation->mScalingKeys[0].mValue;
        const aiVector3D& lastFrameValue =
                nodeAnimation->mScalingKeys[nodeAnimation->mNumScalingKeys - 1].mValue;

        if (animationTime < firstFrameTime) {
            scaling = firstFrameValue;
        } else if (animationTime > lastFrameTime) {
            scaling = lastFrameValue;
        }
    }
}

void MeshAnimation::getRotationFromTime(aiQuaternion& rotation, float animationTime,
        const aiNodeAnim* nodeAnimation) {

    if (nodeAnimation->mNumRotationKeys == 1) {
        rotation = nodeAnimation->mRotationKeys[0].mValue;
        return;
    }

    int index = getRotationIndex(animationTime, nodeAnimation);
    int nextIndex = (index + 1);

    if ((nodeAnimation->mRotationKeys[index].mTime <= animationTime)
            && (animationTime <= nodeAnimation->mRotationKeys[nextIndex].mTime)) {

        float deltaTime = (float) (nodeAnimation->mRotationKeys[nextIndex].mTime
                - nodeAnimation->mRotationKeys[index].mTime);
        float factor = (animationTime - (float) nodeAnimation->mRotationKeys[index].mTime)
                / deltaTime;

        const aiQuaternion& start = nodeAnimation->mRotationKeys[index].mValue;
        const aiQuaternion& end = nodeAnimation->mRotationKeys[nextIndex].mValue;

        aiQuaternion::Interpolate(rotation, start, end, factor);
        rotation = rotation.Normalize();
    } else {
        float firstFrameTime = nodeAnimation->mRotationKeys[0].mTime;
        float lastFrameTime =
                nodeAnimation->mRotationKeys[nodeAnimation->mNumRotationKeys - 1].mTime;
        const aiQuaternion& firstFrameValue = nodeAnimation->mRotationKeys[0].mValue;
        const aiQuaternion& lastFrameValue =
                nodeAnimation->mRotationKeys[nodeAnimation->mNumRotationKeys - 1].mValue;

        if (animationTime < firstFrameTime) {
            rotation = firstFrameValue;
        } else if (animationTime > lastFrameTime) {
            rotation = lastFrameValue;
        }
    }
}

void MeshAnimation::getTranslationFromTime(aiVector3D& translation, float animationTime,
        const aiNodeAnim* nodeAnimation) {

    if (nodeAnimation->mNumPositionKeys == 1) {
        translation = nodeAnimation->mPositionKeys[0].mValue;
        return;
    }

    int index = getTranslationIndex(animationTime, nodeAnimation);
    int nextIndex = (index + 1);

    if ((nodeAnimation->mPositionKeys[index].mTime <= animationTime)
            && (animationTime <= nodeAnimation->mPositionKeys[nextIndex].mTime)) {

        float deltaTime = (float) (nodeAnimation->mPositionKeys[nextIndex].mTime
                        - nodeAnimation->mPositionKeys[index].mTime);
        float factor = (animationTime - (float) nodeAnimation->mPositionKeys[index].mTime)
                / deltaTime;

        const aiVector3D& start = nodeAnimation->mPositionKeys[index].mValue;
        const aiVector3D& end = nodeAnimation->mPositionKeys[nextIndex].mValue;
        aiVector3D delta = end - start;

        translation = start + factor * delta;
    } else {
        float firstFrameTime = nodeAnimation->mPositionKeys[0].mTime;
        float lastFrameTime =
                nodeAnimation->mPositionKeys[nodeAnimation->mNumPositionKeys - 1].mTime;
        const aiVector3D& firstFrameValue = nodeAnimation->mPositionKeys[0].mValue;
        const aiVector3D& lastFrameValue =
                nodeAnimation->mPositionKeys[nodeAnimation->mNumPositionKeys - 1].mValue;

        if (animationTime < firstFrameTime) {
            translation = firstFrameValue;
        } else if (animationTime > lastFrameTime) {
            translation = lastFrameValue;
        }
    }
}

int MeshAnimation::getScalingIndex(float animationTime, const aiNodeAnim* nodeAnimation) {

    for (int i = 0; i < (nodeAnimation->mNumScalingKeys - 1); i++) {
        if (animationTime < (float) nodeAnimation->mScalingKeys[i + 1].mTime) {
            return i;
        }
    }

    return 0;
}

int MeshAnimation::getRotationIndex(float animationTime, const aiNodeAnim* nodeAnimation) {

    for (int i = 0; i < (nodeAnimation->mNumRotationKeys - 1); i++) {
        if (animationTime < (float) nodeAnimation->mRotationKeys[i + 1].mTime) {
            return i;
        }
    }

    return 0;
}

int MeshAnimation::getTranslationIndex(float animationTime, const aiNodeAnim* nodeAnimation) {

    for (int i = 0; i < (nodeAnimation->mNumPositionKeys - 1); i++) {
        if (animationTime < (float) nodeAnimation->mPositionKeys[i + 1].mTime) {
            return i;
        }
    }

    return 0;
}

}
