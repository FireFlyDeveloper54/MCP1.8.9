"""Adapt the unmodified Realms 1.7.59 jar to MCP1.8.9's GLFW/GL3 bridges.

Only CONSTANT_Utf8 class owners and descriptors are changed. Every jar entry,
method body, resource, and service implementation is retained.
"""

import argparse
import hashlib
import json
import struct
from pathlib import Path
from zipfile import ZipFile


SOURCE_SHA256 = "4a6a90ed956609d6cd3a7f84622fcac87d22e9a5d4858c6d1e965af860d07a63"
OWNERS = {
    "org/lwjgl/input/Keyboard": "net/minecraft/realms/RealmsKeyboard",
    "org/lwjgl/input/Mouse": "net/minecraft/realms/RealmsMouse",
    "org/lwjgl/opengl/GL11": "net/minecraft/realms/RealmsGL11",
    "org/lwjgl/opengl/GL13": "net/minecraft/realms/RealmsGL13",
    "org/lwjgl/opengl/ARBMultitexture": "net/minecraft/realms/RealmsARBMultitexture",
    "org/lwjgl/opengl/GLContext": "net/minecraft/realms/RealmsGLContext",
    "org/lwjgl/opengl/ContextCapabilities": "net/minecraft/realms/RealmsCapabilities",
    "com/google/common/util/concurrent/Futures": "net/minecraft/realms/RealmsFutures",
}
SIZES = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4, 11: 4,
         12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}


def adapt_class(data):
    if data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError("Not a Java class")
    count = struct.unpack_from(">H", data, 8)[0]
    result = bytearray(data[:10])
    offset = 10
    index = 1
    replacements = 0
    while index < count:
        tag = data[offset]
        offset += 1
        result.append(tag)
        if tag == 1:
            size = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            value = data[offset:offset + size]
            offset += size
            for old, new in OWNERS.items():
                old_bytes, new_bytes = old.encode("ascii"), new.encode("ascii")
                replacements += value.count(old_bytes)
                value = value.replace(old_bytes, new_bytes)
            result.extend(struct.pack(">H", len(value)))
            result.extend(value)
        else:
            if tag not in SIZES:
                raise ValueError(f"Unknown constant pool tag {tag}")
            size = SIZES[tag]
            result.extend(data[offset:offset + size])
            offset += size
            if tag in (5, 6):
                index += 1
        index += 1
    result.extend(data[offset:])
    return bytes(result), replacements


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    if args.source.resolve() == args.output.resolve():
        parser.error("Source and output must be different files")
    source_hash = hashlib.sha256(args.source.read_bytes()).hexdigest()
    if source_hash != SOURCE_SHA256:
        raise ValueError(f"Unexpected source SHA256: {source_hash}")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    changed = {}
    class_count = 0
    with ZipFile(args.source) as source, ZipFile(args.output, "w") as target:
        target.comment = source.comment
        for entry in source.infolist():
            data = source.read(entry.filename)
            if entry.filename.endswith(".class"):
                class_count += 1
                data, count = adapt_class(data)
                if count:
                    changed[entry.filename] = count
            target.writestr(entry, data)
    output_hash = hashlib.sha256(args.output.read_bytes()).hexdigest()
    audit = {
        "source": "com.mojang:realms:1.7.59",
        "source_sha256": source_hash,
        "output_sha256": output_hash,
        "classes_retained": class_count,
        "owner_rules": OWNERS,
        "classes_adapted": changed,
        "method_bodies_changed": False,
        "entries_removed": [],
    }
    args.output.with_suffix(".adaptation.json").write_text(
        json.dumps(audit, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Retained {class_count} classes; adapted {len(changed)} classes")
    print(f"Output SHA256: {output_hash}")


if __name__ == "__main__":
    main()
