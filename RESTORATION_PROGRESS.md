# MCP / Naven restoration progress

## Objective and order

1. Restore Minecraft 1.8.9 behavior and visuals in MCP1.8.9, including every confirmed audit defect and capabilities removed from the original client.
2. Commit the restoration before making further improvements.
3. Improve MCP1.8.9 while retaining the restored behavior.
4. Apply the improved MCP foundation to Naven main, noauth, and Recode-NoAuth, preserving their intentional client modules and making default MCP behavior match vanilla.
5. Verify each source tree and branch against these requirements before completing the goal.

## Sources and constraints

- MCP starting revision: 67e4a8c176796f77eb9b870aeeac19f1dbe963aa.
- Naven starting revisions: main 0d1155f22223fb7eb1e2984dbdb28b6d8d29f8ed; noauth 1f898cb5925ecdc69e4382a9398e3848fd499c57; Recode-NoAuth 0a215a0049057c5efaba7320276aa4d219536680.
- Source reference: C:/Users/Admin/Downloads/MCP-919-main; shared decompiler errors require checking the original 1.8.9 bytecode.
- Preserve the existing uncommitted MCP Start.java change.
- Preserve logging and diagnostics.
- Current MCP platform: Java 21, IDEA source roots, MCP named 1.8.9, LWJGL 3.3.6, OpenGL 3.3 core.
- Naven platform: Maven Java 21, branch-specific LWJGL 2 / LWJGL 3 compatibility integration.

## Restoration work in progress

| Area | Required result | State |
|---|---|---|
| Core GL state | Correct VBO bindings, model brightness, texture matrices, TexGen, shader translation, fog, overlay, normal and attrib state | Source fixes compiled; runtime effects unverified |
| Render callers / window | Correct shader vertex data, stream offsets, cloud colors, fullscreen modes, merged input and actual startup dimensions | Source fixes compiled; runtime behavior unverified |
| World / entities | Correct portal coordinates, lighting, spawning, chunk lifecycle, culling and default autosave | Source fixes present |
| Storage | Restore the original saveExtraData loop exit missing from both decompiled source trees | Source restored using original bytecode |
| Sound | Restore streaming playback, stopped-channel reuse, failure rollback and original channel allocation | Source fixes compiled; playback unverified |
| Math / fonts / resources | Vanilla trig and square root behavior, Unicode detection, complete reload notifications, mipmap and particle defaults | Source fixes compiled; bounded font review complete |
| Removed capabilities | Restore original Realms, stream and Snooper interfaces and their reachable client integration | Source and dependencies restored; online/runtime behavior unverified |
| Validation / restoration commit | Evidence for the full restored scope, then a separate restoration commit | Audited source restoration committed as 0a0a32b; runtime verification pending |
| MCP improvements | Separate changes after the restoration commit, preserving behavior | Shader failure cleanup compiled; separate commit ready |
| Naven main / noauth / Recode-NoAuth | Restore affected MCP behavior using the improved MCP implementation | Pending |

This file records work, not proof of completion. Commit IDs and validation evidence will be added as the corresponding stages are completed.

## Evidence so far

- Six full Java 21 compilation attempts have been recorded privately; attempts 2–6 exited successfully. Attempts 5 and 6 include all 2006 current Java files and ended with 0 errors / 6 warnings.
- Static Realms linkage: 154 classes retained; 3298 non-JDK member references examined; 0 unresolved names/descriptors. Access rules, initialization and online availability are outside this check.
- Existing Start.java diff and logging configuration are preserved.
- No tests or game/runtime launch have been performed.
- Continued findings and repair scope: [MCP_RESTORATION_AUDIT_2026-10-05.md](C:/Users/Admin/ideaProject/MCP1.8.9/MCP_RESTORATION_AUDIT_2026-10-05.md).

## Commits and separate improvement stage

- Restoration: `0a0a32b9911490d025bb3d43585a55cd60fa62d1` — Restore audited vanilla behavior and client services on GL3. 144 files; Start.java excluded.
- Subsequent improvement: release shader/program objects if shader compilation or linking fails. Successful rendering behavior and diagnostic messages are preserved. The sixth full compilation exited 0 with the same 6 warnings; separate commit ready.
