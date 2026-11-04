package io.github.shek014.openlift.ui.body

import io.github.shek014.openlift.data.model.Muscle

enum class BodyView { Front, Back }

/**
 * One shape on the body map. Paths are SVG path data on a canvas of [BodyMapWidth] by
 * [BodyMapHeight], drawn for the left half of the picture. The figure mirrors each one across the
 * middle. A null [muscle] is a part of the body that doesn't highlight, such as the head.
 */
class BodyMapRegion(val view: BodyView, val muscle: Muscle?, val path: String)

const val BodyMapWidth = 200f
const val BodyMapHeight = 420f

private const val Head = "M100 8 C88 8 83 18 83 30 C83 42 90 52 100 52 C110 52 117 42 117 30 C117 18 112 8 100 8 Z"
private const val Hand = "M36 206 L46 208 C47 216 46 224 43 232 C39 234 34 232 32 226 C32 218 33 212 36 206 Z"
private const val Forearm = "M46 147 L59 150 C58 168 52 188 46 204 L36 202 C35 184 39 164 46 147 Z"
private const val SideDelt = "M68 67 C58 69 51 78 50 96 L56 102 C57 88 62 76 72 69 Z"

val BodyMapRegions: List<BodyMapRegion> = listOf(
    // Front
    BodyMapRegion(BodyView.Front, null, Head),
    BodyMapRegion(BodyView.Front, null, "M100 52 L91 50 L91 60 L100 66 Z"),
    BodyMapRegion(BodyView.Front, null, Hand),
    BodyMapRegion(BodyView.Front, null, "M72 298 L90 298 C91 330 87 368 84 394 L72 394 C67 368 66 330 72 298 Z"),
    BodyMapRegion(BodyView.Front, null, "M72 396 L84 396 L86 412 L66 412 C66 406 69 400 72 396 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Traps, "M91 56 L72 67 L88 70 L100 68 L100 66 Z"),
    BodyMapRegion(BodyView.Front, Muscle.SideDelts, SideDelt),
    BodyMapRegion(BodyView.Front, Muscle.FrontDelts, "M72 69 C62 76 57 88 56 102 L68 100 C69 90 75 78 84 71 Z"),
    BodyMapRegion(BodyView.Front, Muscle.UpperChest, "M100 70 L84 71 C76 76 71 84 69 92 L100 88 Z"),
    BodyMapRegion(BodyView.Front, Muscle.MidChest, "M69 92 L100 88 L100 104 L68 107 C67 102 68 97 69 92 Z"),
    BodyMapRegion(BodyView.Front, Muscle.LowerChest, "M68 107 L100 104 L100 120 C88 125 74 122 68 107 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Biceps, "M56 104 L68 102 C68 116 65 132 60 146 L47 143 C47 128 50 114 56 104 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Forearms, Forearm),
    BodyMapRegion(BodyView.Front, Muscle.Abs, "M100 124 C94 125 89 125 84 124 L83 196 C88 206 94 212 100 214 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Obliques, "M82 124 C76 124 71 121 67 116 L65 150 C63 170 65 186 69 198 L81 194 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Abductors, "M68 200 C62 206 60 218 61 236 C62 246 63 252 65 258 C65 238 66 222 70 206 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Quads, "M71 206 C66 232 65 264 72 296 L90 296 C91 266 92 240 91 218 C83 216 77 212 71 206 Z"),
    BodyMapRegion(BodyView.Front, Muscle.Adductors, "M100 218 L93 218 C94 236 94 256 92 272 C96 262 100 240 100 222 Z"),

    // Back
    BodyMapRegion(BodyView.Back, null, Head),
    BodyMapRegion(BodyView.Back, null, Hand),
    BodyMapRegion(BodyView.Back, null, "M72 382 L88 382 L84 396 L72 396 Z"),
    BodyMapRegion(BodyView.Back, null, "M72 398 L84 398 L86 412 L66 412 C66 406 69 402 72 398 Z"),
    BodyMapRegion(BodyView.Back, Muscle.Traps, "M100 50 L91 54 C88 60 80 64 70 68 L84 78 L100 86 Z"),
    BodyMapRegion(BodyView.Back, Muscle.SideDelts, SideDelt),
    BodyMapRegion(BodyView.Back, Muscle.RearDelts, "M72 69 C62 76 57 88 56 102 L70 101 C72 92 77 84 84 79 Z"),
    BodyMapRegion(BodyView.Back, Muscle.MidBack, "M100 88 L84 80 C77 86 73 95 71 104 L84 116 L100 134 Z"),
    BodyMapRegion(BodyView.Back, Muscle.Lats, "M70 106 C66 120 65 140 68 168 C75 176 88 178 100 178 L100 136 L83 118 Z"),
    BodyMapRegion(BodyView.Back, Muscle.LowerBack, "M100 180 C88 180 76 178 68 171 L70 200 C80 204 90 206 100 206 Z"),
    BodyMapRegion(BodyView.Back, Muscle.Triceps, "M56 104 L69 103 C69 116 66 132 60 146 L47 143 C47 128 50 114 56 104 Z"),
    BodyMapRegion(BodyView.Back, Muscle.Forearms, Forearm),
    BodyMapRegion(BodyView.Back, Muscle.Glutes, "M100 208 C88 208 76 206 68 203 C60 216 60 236 66 250 C78 257 92 255 100 248 Z"),
    BodyMapRegion(BodyView.Back, Muscle.Hamstrings, "M66 254 C64 276 66 296 72 316 L92 316 C94 296 96 274 96 254 C86 260 76 260 66 254 Z"),
    BodyMapRegion(BodyView.Back, Muscle.Calves, "M72 320 C64 338 64 362 71 380 L89 380 C93 362 95 338 91 320 Z"),
)
