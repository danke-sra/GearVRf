/*
 * memory_file.h
 *
 *  Created on: Dec 17, 2015
 *      Author: Danke Xie
 */

#ifndef CONTRIB_JASSIMP2_MEMORY_FILE_H_
#define CONTRIB_JASSIMP2_MEMORY_FILE_H_

#include <stddef.h>
#include <assimp/cfileio.h>

struct MemoryFileData {
    unsigned char *buf;
    size_t size;
    size_t pos;
};

extern aiFile memoryFilePrototype;

#endif /* CONTRIB_JASSIMP2_MEMORY_FILE_H_ */
