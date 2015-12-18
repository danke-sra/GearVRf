/*
 * memory_file.c
 *
 *  Created on: Dec 17, 2015
 *      Author: Danke Xie
 */

#include "memory_file.h"

#ifdef JNI_LOG
#ifdef ANDROID
#include <android/log.h>
#define lprintf(...) __android_log_print(ANDROID_LOG_VERBOSE, __func__, __VA_ARGS__)
#else
#define lprintf(...) printf (__VA_ARGS__)
#endif /* ANDROID */
#else
#define lprintf
#endif

// Memory File

static size_t memoryFileWriteProc(C_STRUCT aiFile* memFile, const char* buf, size_t, size_t) {
    lprintf("Memory file cannot be written to.");
    return 0;
}

static size_t memoryFileReadProc(C_STRUCT aiFile* memFile, char* pvBuffer, size_t pSize, size_t pCount) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));

    const size_t cnt = std::min(pCount, (fileData.size - fileData.pos) / pSize);
    const size_t ofs = pSize * cnt;

    memcpy(pvBuffer, fileData.buf + fileData.pos, ofs);
    fileData.pos += ofs;

    return cnt;
}

static size_t memoryFileTellProc(C_STRUCT aiFile* memFile) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));
    return fileData.pos;
}

static size_t memoryFileSizeProc(C_STRUCT aiFile* memFile) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));
    return fileData.size;
}

static void memoryFileFlushProc(C_STRUCT aiFile* memFile) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));
    lprintf("Memory file cannot be written/flushed.");
}

static aiReturn memoryFileSeek(C_STRUCT aiFile* memFile, size_t pOffset, aiOrigin pOrigin) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));

    if (aiOrigin_SET == pOrigin) {
        if (pOffset >= fileData.size) {
            return aiReturn_FAILURE;
        }
        fileData.pos = pOffset;
    }
    else if (aiOrigin_END == pOrigin) {
        if (pOffset >= fileData.size) {
            return aiReturn_FAILURE;
        }
        fileData.pos = fileData.size - pOffset;
    }
    else {
        if (pOffset + fileData.pos >= fileData.size) {
            return aiReturn_FAILURE;
        }
        fileData.pos += pOffset;
    }
    return aiReturn_SUCCESS;
}

aiFile memoryFilePrototype {
    .ReadProc = memoryFileReadProc,
    .WriteProc = memoryFileWriteProc,
    .TellProc = memoryFileTellProc,
    .FileSizeProc = memoryFileSizeProc,
    .SeekProc = memoryFileSeek,
    .FlushProc = memoryFileFlushProc,
    .UserData = nullptr
};
